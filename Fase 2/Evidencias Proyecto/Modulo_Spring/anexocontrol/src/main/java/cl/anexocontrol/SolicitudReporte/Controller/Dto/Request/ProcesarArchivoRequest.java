package cl.anexocontrol.SolicitudReporte.Controller.Dto.Request;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class ProcesarArchivoRequest {
    private Long idSolicitud;
    private Long idCarga;
    private Long idUsuario;
    private String rutaArchivo;
    private Long idTipoArchivo;
}
