package cl.anexocontrol.SolicitudReporte.Repository.Jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// mapeo de las solicitudes de archivos pdf o csv
@Entity
@Table(name = "solicitud_reporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudReporteJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_solicitud_reporte")
    @SequenceGenerator(name = "seq_solicitud_reporte", sequenceName = "SEQ_SOLICITUD_REPORTE", allocationSize = 1)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "estado_solicitud")
    private String estadoSolicitado;

    @Column(name = "ruta_reporte")
    private String rutaReporte;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_tipo_reporte")
    private Integer idTipoReporte;
}
