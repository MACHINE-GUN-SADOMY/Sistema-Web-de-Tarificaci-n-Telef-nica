package cl.anexocontrol.SolicitudReporte.Client;

import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesarArchivoRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProcesamientoClient { // usamos restclient para consumir externos de http
    private final RestClient restClient;

    // builder construira la base del endpoint (http)
    public ProcesamientoClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8000").build();
    }

    // el endpoint
    public void notificarArchivoListo(ProcesarArchivoRequest request){
        restClient.post().uri("/api/procesamiento/archivo-listo/")
                .body(request).retrieve().toBodilessEntity();
    } // body(request) convierte el objeto a json
    // el bodyess sirve para que spring no lea el cuerpo de la response, solo saber si la call fue exitosa
}
