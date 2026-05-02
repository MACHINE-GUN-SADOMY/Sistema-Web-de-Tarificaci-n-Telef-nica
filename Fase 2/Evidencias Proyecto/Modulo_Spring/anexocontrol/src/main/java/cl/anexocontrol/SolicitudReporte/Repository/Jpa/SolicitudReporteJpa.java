package cl.anexocontrol.SolicitudReporte.Repository.Jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "estado_solicitado")
    private String estadoSolicitado;

    @Column(name = "ruta_reporte")
    private String rutaReporte;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_tipo_reporte")
    private Integer idTipoReporte;
}
