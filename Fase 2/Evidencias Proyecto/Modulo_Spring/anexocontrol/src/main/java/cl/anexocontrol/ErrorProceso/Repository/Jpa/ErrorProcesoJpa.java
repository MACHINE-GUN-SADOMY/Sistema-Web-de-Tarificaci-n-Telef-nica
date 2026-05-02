package cl.anexocontrol.ErrorProceso.Repository.Jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// aca registramos si algo falla en la base o el codigo
@Entity
@Table(name = "error_proceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorProcesoJpa {

    @Id
    @Column(name = "id_error")
    private Long idError;

    @Column(name = "modulo_origen")
    private String moduloOrigen;

    @Column(name = "procedimiento_origen")
    private String procedimientoOrigen;

    @Column(name = "mensaje_error")
    private String mensajeError;

    @Column(name = "fecha_error")
    private LocalDateTime fechaError;

    // estos campos pueden ser null si el error es muy general
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "id_registro_llamada")
    private Long idRegistroLlamada;

    @Column(name = "detalle_error")
    private String detalleError;
}
