package cl.anexocontrol.SolicitudReporte.Controller.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudReporteRequest {
    private Long idSolicitud;
    private Long idCarga;
    private LocalDateTime fechaSolicitud;
    private String estadoSolicitado;
    private String rutaReporte;
    private Long idUsuario;
    private Integer idTipoReporte;
}
