package cl.anexocontrol.ReporteTarificacion.Repository.Jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// esta tabla tiene los resultados de la tarificacion de oracle
@Entity
@Table(name = "reporte_tarificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTarificacionJpa {

    @Id
    @Column(name = "id_reporte_tarificacion")
    private Long idReporteTarificacion;

    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "anexo")
    private String anexo;

    @Column(name = "duracion_total_segundos")
    private Long duracionTotalSegundos;

    @Column(name = "proveedor")
    private String proveedor;

    // usamos integer para el costo total
    @Column(name = "costo_calculado")
    private Integer costoCalculado;

    @Column(name = "fecha_procesado")
    private LocalDateTime fechaProcesado;

    @Column(name = "cant_llamadas_usuario")
    private Long cantLlamadasUsuario;

    @Column(name = "cant_total_llamadas_usuario")
    private Long cantTotalLlamadasUsuario;

    @Column(name = "total_tiempo_carga")
    private Long totalTiempoCarga;

    @Column(name = "prom_duracion_llamada")
    private Integer promDuracionLlamada;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_registro_llamada")
    private Long idRegistroLlamada;

    @Column(name = "id_tipo_llamada")
    private Integer idTipoLlamada;
}
