import requests
from rest_framework.exceptions import ValidationError

class SpringCallbackService:
    # la url de spring pa devolverle el estado
    SPRING_CALLBACK_URL = "http://localhost:8081/procesamiento/callback"

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