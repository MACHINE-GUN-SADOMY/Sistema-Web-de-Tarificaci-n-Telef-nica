import requests
from rest_framework.exceptions import ValidationError

class SpringCallbackService:
    SPRING_CALLBACK_URL = "http://localhost:8081/procesamiento/callback"

    def notificarSolicitudLista(self, idSolicitud: int, rutaReporte: str):
        body = {
            "idSolicitud": idSolicitud,
            "estadoSolicitado": "LISTO",
            "rutaReporte": rutaReporte,
            "mensajeError": None
        }

        response = requests.post(
            self.SPRING_CALLBACK_URL,
            json=body,
            timeout=10
        )

        if response.status_code < 200 or response.status_code >= 300:
            raise ValidationError(
                f"No se pudo notificar a Spring. Status: {response.status_code}, Respuesta: {response.text}"
            )

        return response.json()