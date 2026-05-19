# importamos pandas para manejar tablas de datos y rest framework para controlar los errores de validacion
import pandas as pd
from rest_framework import serializers
from .models import RegistroLlamada,TipoLlamada,Usuario

# esta clase contiene el motor principal para parsear, higienizar y transformar los registros de llamadas crudas.
# la separamos del resto de la aplicacion porque las reglas de negocio sobre como limpiar un csv de llamadas
# (como eliminar espacios o validar columnas) cambian frecuentemente y no queremos ensuciar los controladores o la api de django.
class ProcesadorLlamadasService:
    # definimos estas propiedades de clase para almacenar temporalmente los datos intermedios del procesamiento de una carga.
    # esto nos permite dividir el procesamiento en pequeños metodos enfocados (lectura, limpieza, normalizacion)
    # sin tener que estar pasando el dataframe de un metodo a otro constantemente como argumento.
    rutaArchivo = None
    dataFrame = None
    registroLimpio = []
    tipoLlamada = None

    # estas columnas son el contrato minimo que exige nuestro software para poder calcular la tarificacion y costos.
    # si falta una sola de estas, el algoritmo de tarifas no podria calcular los costos por proveedor o duracion,
    # por lo que es preferible abortar el proceso antes de registrar datos huerfanos o inservibles.
    COLUMNAS_REQUERIDAS = [
        "anexo",
        "numero_destino",
        "duracion_segundos",
        "proveedor",
        "fecha_llamada",
        "id_tipo_llamada"]

    # aca cargamos el archivo al sistema segun sea csv o txt. el software de la central telefonica suele exportar
    # en ambos formatos, asi que le damos flexibilidad al usuario final. usamos pandas porque leer archivos linea por linea en python
    # puro seria muy lento y consumiria demasiada cpu si el archivo tiene mas de diez mil registros de llamadas.
    def leerArchivo(self,rutaArchivo:str):
        self.rutaArchivo = rutaArchivo

        # las centrales telefonicas mas modernas exportan en csv nativo. pandas lo lee de un viaje y optimiza los tipos de datos de forma automatica.
        if rutaArchivo.lower().endswith(".csv"):
            self.dataFrame = pd.read_csv(rutaArchivo)
            return self.dataFrame

        # algunos modelos antiguos de centrales exportan en formato txt delimitado por comas.
        # lo leemos igual con read_csv pasandole la coma como separador explito para reutilizar la velocidad de pandas sin duplicar logica.
        if rutaArchivo.lower().endswith(".txt"):
            self.dataFrame = pd.read_csv(rutaArchivo, sep=',') 
            return self.dataFrame

        # si nos suben un pdf, excel o cualquier otra cosa extraña, paramos el proceso tirando un error de validacion rest.
        # esto evita que procesemos basura que podria romper la memoria del servidor.
        raise serializers.ValidationError("Formato de archivo no valido")

    # este metodo es un control de seguridad estructural. valida que el archivo contenga las columnas correctas 
    # y que realmente traiga informacion antes de pasar a la etapa de limpieza de datos.
    def validarEstructura(self,dataFrame):
        columnas_archivo = list(dataFrame.columns)

        # verificamos contra nuestro arreglo estatico de columnas requeridas.
        # si falta una columna clave (como numero de destino o duracion), el proceso fallara en la base de datos, 
        # por lo que preferimos avisarle al usuario de inmediato cual columna falta para que arregle su excel.
        for columna in self.COLUMNAS_REQUERIDAS:
            if columna not in columnas_archivo:
                raise serializers.ValidationError(f"La columna {columna} es obligatoria")

            # un archivo que solo tiene cabeceras pero ninguna llamada es inutil para el sistema de costos.
            # levantamos un error para evitar crear registros de carga vacios en la base de datos de auditoria.
            if dataFrame.empty:
                raise serializers.ValidationError("El archivo no contiene registros")
        return True

    # aca hacemos el proceso de higienizacion de los datos. los archivos planos exportados de las centrales telefonicas
    # suelen venir con imperfecciones de formato, saltos de linea vacios o registros repetidos por fallos de sincronizacion de red.
    def limpiarDatos(self,dateFrame):
        # las lineas completas en blanco suelen quedar al final de los archivos exportados. las eliminamos todas
        # de una para evitar errores de tipo null al intentar convertirlas a enteros o fechas.
        dataframe = dateFrame.dropna(how='all')
        
        # las llamadas duplicadas pueden ocurrir por reintentos de conexion en la central.
        # si dejamos llamadas repetidas en el archivo, se le cobrara el doble al cliente o al area, lo que alteraria gravemente la facturacion.
        dataframe = dataframe.drop_duplicates()

        # limpiamos espacios en blanco invisibles al inicio y al final de los textos (strip).
        # esto es vital porque campos como 'anexo' o 'proveedor' se buscan en base de datos, y un espacio extra (como ' anexo1' o 'claro ')
        # haria que las busquedas relacionales fallen silenciosamente.
        for columna in dataframe.columns:
            if dataframe[columna].dtype == 'object':
                dataframe[columna] = dataframe[columna].astype(str).str.strip()

        # actualizamos el dataframe de la instancia para que los metodos posteriores consuman la version higienizada.
        self.dataFrame = dataframe

        return dataframe

    # en este paso transformamos los datos planos del dataframe a instancias del modelo de django (registrollamada).
    # ademas, hacemos las validaciones de negocio e integridad referencial cruzando los datos contra las tablas reales de la base de datos.
    def normalizarDatos(self,dataFrame,idCarga,idUsuario):
        self.idCarga = idCarga
        self.registrosLimpios = []

        # antes de recorrer miles de filas, validamos si el usuario que esta haciendo la carga realmente existe en el sistema.
        # si no existe, paramos todo de inmediato porque todas las llamadas deben asociarse a una cuenta de usuario valida para trazabilidad.
        usuario = Usuario.objects.filter(pk=idUsuario).first()

        if usuario is None:
            raise serializers.ValidationError("La usuario no existe")

        # recorremos cada llamada del archivo para mapear sus campos.
        for _, fila in dataFrame.iterrows():
            # buscamos si el identificador del tipo de llamada (por ejemplo celular, local, internacional) existe en nuestra base de datos.
            # esto evita que registremos llamadas clasificadas de forma incorrecta, lo que arruinaria los reportes estadisticos de facturacion.
            tipo_llamada = TipoLlamada.objects.filter(pk=int(fila["id_tipo_llamada"])).first()

            if tipo_llamada is None:
                raise serializers.ValidationError(f"El tipo_llamada {fila['id_tipo_llamada']} no existe")

            # creamos el objeto django en memoria. casteamos y estandarizamos cada campo segun los requisitos estrictos de la bd
            # (proveedores siempre en mayusculas, anexos como strings limpios, fechas convertidas a formato date nativo de python, etc.).
            registro = RegistroLlamada(
                id_carga=idCarga,
                anexo=str(fila["anexo"]),
                numero_destino=int(fila["numero_destino"]),
                duracion_segundos=int(fila["duracion_segundos"]),
                proveedor=str(fila["proveedor"]).upper(),
                fecha_llamada=pd.to_datetime(fila["fecha_llamada"]).date(),
                id_usuario=usuario,
                id_tipo_llamada=tipo_llamada)

            # guardamos el registro estructurado en una lista temporal en memoria.
            # no hacemos .save() aca para evitar enviar miles de queries individuales de insert a la base de datos dentro de este bucle.
            self.registrosLimpios.append(registro)

        # retornamos toda la coleccion de llamadas listas y estandarizadas para que puedan guardarse eficientemente en lote en la base de datos.
        return self.registrosLimpios
