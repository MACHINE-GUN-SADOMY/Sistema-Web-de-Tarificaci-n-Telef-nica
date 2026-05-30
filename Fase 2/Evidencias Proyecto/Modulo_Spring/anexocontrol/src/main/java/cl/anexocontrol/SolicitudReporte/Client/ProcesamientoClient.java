package cl.anexocontrol.SolicitudReporte.Client;

import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesarArchivoRequest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProcesamientoClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ProcesamientoClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder
                .baseUrl("http://localhost:8000")
                .build();

        this.objectMapper = objectMapper;
    }

    public void notificarArchivoListo(ProcesarArchivoRequest request) {
        try {
            Map<String, Object> bodyMap = new LinkedHashMap<>();
            bodyMap.put("idSolicitud", request.getIdSolicitud());
            bodyMap.put("idCarga", request.getIdCarga());
            bodyMap.put("idUsuario", request.getIdUsuario());
            bodyMap.put("idTipoReporte", request.getIdTipoReporte());
            bodyMap.put("rutaArchivo", request.getRutaArchivo());

            String jsonBody = objectMapper.writeValueAsString(bodyMap);

            restClient.post()
                    .uri("/procesar-archivo-listo/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException exception) {
            throw new RuntimeException("Error desde Django: " + exception.getResponseBodyAsString());

        } catch (Exception exception) {
            throw new RuntimeException("Error preparando request hacia Django: " + exception.getMessage());
        }
    }
}
