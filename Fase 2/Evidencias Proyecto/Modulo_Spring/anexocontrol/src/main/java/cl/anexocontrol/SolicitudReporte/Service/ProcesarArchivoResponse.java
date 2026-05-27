package cl.anexocontrol.SolicitudReporte.Service;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class ProcesarArchivoResponse {
    private String mensaje;
    private Long id_solicitud;
    private Long id_carga;
    private Integer registrosInsertados;
    private String rutaReporte;
}
