package cl.anexocontrol.SolicitudReporte.Controller.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class SolicitudReporteCallbackRequest {
    private Long idSolicitud;
    private String estadoSolicitud;
    private String rutaReporte;
    private String mensajeError;
}
