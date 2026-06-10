import os
import requests
from rest_framework.exceptions import ValidationError

class SpringCallbackService:
    # la url de spring pa devolverle el estado
    # en Docker usar: SPRING_CALLBACK_URL=http://spring-app:8081/procesamiento/callback
    SPRING_CALLBACK_URL = os.getenv(
        "SPRING_CALLBACK_URL",
        "http://localhost:8081/procesamiento/callback"
    )

    # le avisamos a spring que ya esta listo el archivo
    def notificarSolicitudLista(self, idSolicitud: int, rutaReporte: str):
        # armamos lo que vamos a mandar
        body = {
            "idSolicitud": idSolicitud,
            "estadoSolicitado": "LISTO",
            "rutaReporte": rutaReporte,
            "mensajeError": None
        }

        # hacemos la peticion
        response = requests.post(
            self.SPRING_CALLBACK_URL,
            json=body,
            timeout=10
        )

        # si falla tiramos la bronca
        if response.status_code < 200 or response.status_code >= 300:
            raise ValidationError(
                f"No se pudo notificar a Spring. Status: {response.status_code}, Respuesta: {response.text}"
            )

        # devolvemos lo que respondio
        return response.json()

    # le avisamos a spring que el procesamiento fallo con un mensaje de error
    def notificarError(self, idSolicitud: int, mensajeError: str):
        # rutaReporte se envia como 'ERROR' porque Spring lo requiere en el campo
        # para el caso ERROR: procesarCallback lo lee pero lo sobreescribe internamente
        # con 'ERROR' de todas formas. mensajeError llega al DTO pero Spring no lo
        # persiste actualmente, asi que es solo informativo y no rompe el flujo.
        body = {
            "idSolicitud": idSolicitud,
            "estadoSolicitado": "ERROR",
            "rutaReporte": "ERROR",
            "mensajeError": mensajeError
        }

        # hacemos la peticion
        response = requests.post(
            self.SPRING_CALLBACK_URL,
            json=body,
            timeout=10
        )

        # si falla tiramos la bronca
        if response.status_code < 200 or response.status_code >= 300:
            raise ValidationError(
                f"No se pudo notificar el error a Spring. Status: {response.status_code}, Respuesta: {response.text}"
            )

        return response.json()