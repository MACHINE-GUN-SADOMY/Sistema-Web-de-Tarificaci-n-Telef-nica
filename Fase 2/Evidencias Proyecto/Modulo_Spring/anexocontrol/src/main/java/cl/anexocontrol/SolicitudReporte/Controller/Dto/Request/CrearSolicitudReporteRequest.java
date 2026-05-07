package cl.anexocontrol.SolicitudReporte.Controller.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrearSolicitudReporteRequest {
    private Long idCarga;
    private Long idUsuario;
    private Integer idTipoReporte;
}
