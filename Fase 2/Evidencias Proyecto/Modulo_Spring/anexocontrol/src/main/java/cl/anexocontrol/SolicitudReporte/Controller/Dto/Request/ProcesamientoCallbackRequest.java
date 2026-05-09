package cl.anexocontrol.SolicitudReporte.Controller.Dto.Request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcesamientoCallbackRequest {
    private Long idSolicitud;
    private String estadoSolicitado;
    private String rutaReporte;
}
