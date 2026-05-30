from django.urls import path
from procesamiento.api import *

# aqui declaramos la url completa segun la heredacion con API View
urlpatterns = [
    # ruta pa cuando avisan que el archivo ta listo
    path(
        'procesar-archivo-listo/',
        ProcesamientoLlamadas.as_view(),
        name='procesarArchivo'
    ),

    # ruta pa ver todos los reportes de una
    path(
        'mostrar-tarificaciones/',
        MostrarTodasLasTarificaciones.as_view(),
        name='mostrarTarificaciones'
    ),

    # ruta pa ver un puro reporte especifico
    path(
        'mostrar-tarificacion/<int:id>/',
        MostrarTarifificacionPorIdReporte.as_view(),
        name='mostrarTarificacion'
    ),

    # ruta pa pitearse un reporte
    path(
      'eliminar-tarificacion/<int:id>/',
        EliminarTarificacion.as_view(),
        name='eliminarTarificacion'
    ),
]