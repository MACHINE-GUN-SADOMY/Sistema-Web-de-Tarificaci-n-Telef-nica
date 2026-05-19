import os

from .ProcesadorLlamadasService import ProcesadorLlamadasService
from .orm import (existSolicitudReporteById, existTipoReporteById, existUsuarioById, findSolicitudReporteById,
                  insertarRegistrosLlamada)

# aca coordinamos todo el flujo despues de que el sistema de carga (que viene desde java) nos avisa que el archivo ya esta listo en el servidor.
# la meta aca es procesar el archivo de llamadas telefonicas y meterlo a la base de datos de tarificacion de manera segura.
def procesarArchivoListo(data):
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

    # al terminar, le avisamos al resto del sistema que la carga fue exitosa, devolviendo los ids para actualizar el estado del proceso en la base de datos.
    return {"mensaje": "Archivo recibido correctamente por Django",
            "id_solicitud": id_solicitud,
            "id_carga": id_carga}

# este metodo actua como un escudo de integridad referencial antes de procesar el archivo plano.
# si el usuario que subio el archivo no existe, la solicitud no existe o el tipo de reporte no coincide, el proceso es invalido por negocio.
# tambien nos asegura que el archivo realmente exista en la ruta que nos pasaron para evitar errores de archivo no encontrado (filenotfounderror).
def validarArchivo(id_solicitud: int,id_carga: int,id_usuario: int,id_tipo_reporte: int,ruta_archivo: str):
    # traemos la solicitud de la base de datos usando el repositorio. la necesitamos no solo para validar que exista, 
    # sino tambien para retornarla al final si es que el flujo de validacion pasa correctamente.
    solicitud = findSolicitudReporteById(id_carga)

    # agrupamos todas las condiciones criticas en una sola expresion logica. si falta el usuario, la solicitud, el tipo de reporte,
    # o si el archivo no esta fisicamente en la ruta indicada, retornamos false de inmediato. preferimos esta evaluacion rapida
    # para cortar el flujo temprano (guardia) sin anidar muchos condicionales.
    if (not existUsuarioById(id_usuario) or
        not solicitud or
        not existTipoReporteById(id_tipo_reporte) or
        not os.path.exists(ruta_archivo)):return False

    # si paso todos los controles de negocio y fisicos, devolvemos la solicitud para que el flujo principal pueda continuar su marcha.
    return solicitud
