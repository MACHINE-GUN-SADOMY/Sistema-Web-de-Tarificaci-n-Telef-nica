from django.urls import path
from procesamiento.api import *

# aqui declaramos la url completa segun la heredacion con API View
urlpatterns = [
    path(
        'procesar-archivo-listo/',
        ProcesamientoLlamadas.as_view(),
        name='procesarArchivo'
    ),

    path(
        'mostrar-tarificaciones/',
        MostrarTodasLasTarificaciones.as_view(),
        name='mostrarTarificaciones'
    ),

    path(
        'mostrar-tarificacion/<int:id>/',
        MostrarTarifificacionPorIdReporte.as_view(),
        name='mostrarTarificacion'
    ),

    path(
      'eliminar-tarificacion/<int:id>/',
        EliminarTarificacion.as_view(),
        name='eliminarTarificacion'
    ),
]