from .models import SolicitudReporte, Usuario, TipoReporte, RegistroLlamada


# esto funcinara como un Repository
def findSolicitudReporteById(id_solicitud: int):
    return SolicitudReporte.objects.filter(pk=id_solicitud).first()

def existSolicitudReporteById(id_solicitud: int):
    return SolicitudReporte.objects.filter(pk=id_solicitud).exists()

def existUsuarioById(id_usuario: int):
    return Usuario.objects.filter(pk=id_usuario).exists()

def existTipoReporteById(id_tipo_reporte: int):
    return TipoReporte.objects.filter(pk=id_tipo_reporte).exists()

def findSolicitudByIdCarga(id_carga: int):
    return SolicitudReporte.objects.filter(id_carga=id_carga).first()

def insertarRegistrosLlamada(registros):
    return RegistroLlamada.objects.bulk_create(registros, batch_size=500)