from django.urls import path
from procesamiento.api import *

# aqui declaramos la url completa segun la heredacion con API View
urlpatterns = [
    path(
        'procesar-archivo-listo/',
        ProcesamientoCompletado.as_view(),
        name='api/procesararchivolisto'
    ),
]