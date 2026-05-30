from django.db import connection

from .models import SolicitudReporte, Usuario, TipoReporte, RegistroLlamada, ReporteTarificacion


# esto funcinara como un Repository
# buscar solicitud por su id
def findSolicitudReporteById(id_solicitud: int):
    return SolicitudReporte.objects.filter(pk=id_solicitud).first()

# saber si existe la solicitud
def existSolicitudReporteById(id_solicitud: int):
    return SolicitudReporte.objects.filter(pk=id_solicitud).exists()

# chequear si esta el usuario
def existUsuarioById(id_usuario: int):
    return Usuario.objects.filter(pk=id_usuario).exists()

# ver si el tipo de reporte es de los buenos
def existTipoReporteById(id_tipo_reporte: int):
    return TipoReporte.objects.filter(pk=id_tipo_reporte).exists()

# revisar si hay un reporte tarificado con ese id
def existReporteTarificacionById(id_reporte: int):
    return ReporteTarificacion.objects.filter(pk=id_reporte).exists()

# traer la solicitud usando el id de carga
def findSolicitudByIdCarga(id_carga: int):
    return SolicitudReporte.objects.filter(id_carga=id_carga).first()

# meter todas las llamadas juntas de un viaje
def insertarRegistrosLlamada(registros):
    return RegistroLlamada.objects.bulk_create(registros, batch_size=500)

# de esta forma mediante db django, podemos abrir un cursor y usar el connection para llamar al proc (pr)
def tarificar_carga(id_carga: int):
    with connection.cursor() as cursor:
        cursor.callproc("pkg_tarificacion.pr_tarificar_carga", [id_carga])

# buscar el reporte por id pa mostrarlo
def mostrarTarificacionPorIdReporte(id: int):
    return ReporteTarificacion.objects.filter(id_reporte_tarificacion=id).first()

# traerse todos los reportes de una
def mostrarTodasLasTarificaciones():
    return ReporteTarificacion.objects.all()

# ver reportes filtrados por carga
def mostrarTarificacionPorIdCarga(id_carga: int):
    return ReporteTarificacion.objects.filter(id_carga=id_carga)

# borrar el reporte que se pida
def eliminarReporteTarificacionPorIdReporte(id_reporte: int):
    return ReporteTarificacion.objects.filter(pk=id_reporte).delete()