from django.db import connection

from .models import SolicitudReporte, Usuario, TipoReporte, RegistroLlamada, ReporteTarificacion


# esto funcinara como un Repository
def findSolicitudReporteById(id_solicitud: int):
    return SolicitudReporte.objects.filter(pk=id_solicitud).first()

def existSolicitudReporteById(id_solicitud: int):
    return SolicitudReporte.objects.filter(pk=id_solicitud).exists()

def existUsuarioById(id_usuario: int):
    return Usuario.objects.filter(pk=id_usuario).exists()

def existTipoReporteById(id_tipo_reporte: int):
    return TipoReporte.objects.filter(pk=id_tipo_reporte).exists()

def existReporteTarificacionById(id_reporte: int):
    return ReporteTarificacion.objects.filter(pk=id_reporte).exists()

def findSolicitudByIdCarga(id_carga: int):
    return SolicitudReporte.objects.filter(id_carga=id_carga).first()

def insertarRegistrosLlamada(registros):
    return RegistroLlamada.objects.bulk_create(registros, batch_size=500)

# de esta forma mediante db django, podemos abrir un cursor y usar el connection para llamar al proc (pr)
def tarificar_carga(id_carga: int):
    with connection.cursor() as cursor:
        cursor.callproc("pkg_tarificacion.pr_tarificar_carga", [id_carga])

def mostrarTarificacionPorIdReporte(id: int):
    return ReporteTarificacion.objects.filter(id_reporte_tarificacion=id).first()

def mostrarTodasLasTarificaciones():
    return ReporteTarificacion.objects.all()

def mostrarTarificacionPorIdCarga(id_carga: int):
    return ReporteTarificacion.objects.filter(id_carga=id_carga)

def eliminarReporteTarificacionPorIdReporte(id_reporte: int):
    return ReporteTarificacion.objects.filter(pk=id_reporte).delete()