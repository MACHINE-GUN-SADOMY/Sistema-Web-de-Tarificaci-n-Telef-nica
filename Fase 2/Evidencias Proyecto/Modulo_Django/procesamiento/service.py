import os

from rest_framework import serializers
from .GeneradorReportesService import GeneradorReportesService
from .ProcesadorLlamadasService import ProcesadorLlamadasService
from .SpringCallbackService import SpringCallbackService
from .orm import (existTipoReporteById, existUsuarioById, findSolicitudReporteById,
                  insertarRegistrosLlamada, tarificar_carga, mostrarTodasLasTarificaciones,
                  existReporteTarificacionById, eliminarReporteTarificacionPorIdReporte,
                  mostrarTarificacionPorIdReporte, mostrarTarificacionPorIdCarga)

# Mostrar todas las tarififaciones
def mostrarTarificaciones():
    reportes = mostrarTodasLasTarificaciones()

    if reportes is None:
        raise serializers.ValidationError("No hay reportes")

    return reportes

# mostrara el reporte por id de carga
def mostrarTarificacionPorId(data):
    tarifiacion = mostrarTarificacionPorIdReporte(data["idReporteTarificacion"])

    if tarifiacion is None:
        raise serializers.ValidationError("La carga no existe")

    return tarifiacion

def eliminarReportePorIdReporte(data):
    eliminarReporteTarificacionPorIdReporte(data["idReporteTarificacion"])

    validarReporte = validarIdReporte(data["idReporteTarificacion"])

    if validarReporte is False:
        raise serializers.ValidationError("No hay reporte para eliminar")

    return True

# aca coordinamos todo el flujo despues de que el sistema de carga (que viene desde java) nos avisa que el archivo ya esta listo en el servidor.
# la meta aca es procesar el archivo de llamadas telefonicas y meterlo a la base de datos de tarificacion de manera segura.
def procesarArchivoTarificarCarga(data):
    # desempaquetamos los parametros claves del reporte que nos mandan. los guardamos en variables locales
    # para que sea mas facil leer el codigo y no andar escribiendo data["clave"] a cada rato.
    id_solicitud = data["idSolicitud"]
    id_carga = data["idCarga"]
    id_usuario = data["idUsuario"]
    id_tipo_reporte = data["idTipoReporte"]
    ruta_archivo = data["rutaArchivo"]

    # esta validacion inicial es crucial. antes de que pandas intente abrir el archivo y consuma memoria/ram (lo cual puede ser muy costoso
    # si el archivo es gigante), verificamos que la base de datos realmente conozca al usuario y la solicitud. si falla algo, paramos todo de una
    # para no saturar el servidor procesando basura o datos huerfanos.
    validarArchivo(id_solicitud, id_carga, id_usuario,id_tipo_reporte,ruta_archivo)

    # separamos la logica de parsing y limpieza en una clase aparte (procesadorllamadasservice) para no mezclar la coordinacion del servicio
    # con el trabajo sucio de manipular los datos del archivo. esto hace que el codigo sea mucho mas facil de mantener si cambia el formato del csv.
    procesador = ProcesadorLlamadasService()

    # pandas es el estandar que elegimos aca porque es extremadamente rapido indexando y leyendo archivos planos en memoria.
    dataframe = procesador.leerArchivo(ruta_archivo)
    
    # revisamos que el archivo tenga exactamente lo que el modelo de base de datos exige. si faltan columnas criticas como duracion o destino,
    # no tiene sentido continuar porque la base de datos rechazaria los registros de todas formas por campos nulos obligatorios.
    procesador.validarEstructura(dataframe)

    # la limpieza es obligatoria porque los usuarios suelen subir archivos con lineas vacias al final, llamadas repetidas o espacios extras
    # en los textos. si no limpiamos esto, nos llenariamos de registros duplicados que alterarian los calculos de costos de las llamadas.
    dataframe_limpio = procesador.limpiarDatos(dataframe)

    # convertimos las filas planas del csv en instancias del modelo de django (registrollamada), validando que las llaves foraneas existan.
    # ojo: aca todavia no tocamos la base de datos, solo creamos los objetos en memoria para no saturar la conexion.
    registros_limpios = procesador.normalizarDatos(dataframe_limpio,id_carga,id_usuario)

    # en vez de hacer un insert en la base de datos por cada fila (lo que tomaria minutos y congelaria la app si el archivo tiene miles de llamadas),
    # usamos bulk_create para meter todo de un solo golpe. esto ahorra muchisimo tiempo de conexion y hace que el proceso tome segundos.
    insertarRegistrosLlamada(registros_limpios)

    # luego de insertar los registros, lo que haremos sera llamar al pr, desde la funcion de ORM para tarificar todo
    tarificar_carga(id_carga)

    # aqui creamos un objeto que tenga las tarificaciones
    reportes_tarificados = mostrarTarificacionPorIdCarga(id_carga)

    ruta_reporte = GeneradorReportesService().generarReporte(
        reportes_tarificados,
        id_carga,
        id_tipo_reporte
    )

    SpringCallbackService().notificarSolicitudLista(
        id_solicitud,
        ruta_reporte
    )


    # al terminar, le avisamos al resto del sistema que la carga fue exitosa, devolviendo los ids para actualizar el estado del proceso en la base de datos.
    return {
        "mensaje": "Archivo procesado, tarificado y reporte generado correctamente",
        "id_solicitud": id_solicitud,
        "id_carga": id_carga,
        "registrosInsertados": len(registros_limpios),
        "rutaReporte": ruta_reporte
    }

# este metodo actua como un escudo de integridad referencial antes de procesar el archivo plano.
# si el usuario que subio el archivo no existe, la solicitud no existe o el tipo de reporte no coincide, el proceso es invalido por negocio.
# tambien nos asegura que el archivo realmente exista en la ruta que nos pasaron para evitar errores de archivo no encontrado (filenotfounderror).
def validarArchivo(id_solicitud: int, id_carga: int, id_usuario: int, id_tipo_reporte: int, ruta_archivo: str):
    solicitud = findSolicitudReporteById(id_solicitud)

    if not existUsuarioById(id_usuario):
        raise serializers.ValidationError("El usuario no existe")

    if not solicitud:
        raise serializers.ValidationError("La solicitud de reporte no existe")

    if not existTipoReporteById(id_tipo_reporte):
        raise serializers.ValidationError("El tipo de reporte no existe")

    if not os.path.exists(ruta_archivo):
        raise serializers.ValidationError(f"El archivo no existe en la ruta indicada: {ruta_archivo}")

    return solicitud

# verificar si existe el reporte
def validar(id_reporte: int):
    return existReporteTarificacionById(id_reporte) is not False

# verificar si existe el reporte
def validarIdReporte(id_reporte: int):
    return existReporteTarificacionById(id_reporte) is not False # usariamos none si fuese un objeto no bool
