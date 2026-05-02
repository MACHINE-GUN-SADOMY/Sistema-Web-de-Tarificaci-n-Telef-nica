package cl.anexocontrol.RegistroLlamada.Repository.Jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// aca guardamos los datos limpios que vienen de python
@Entity
@Table(name = "registro_llamada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroLlamadaJpa {

    @Id
    @Column(name = "id_registro_llamada")
    private Long idRegistroLlamada;

    @Column(name = "id_carga")
    private Long idCarga;

    @Column(name = "anexo")
    private String anexo;

    @Column(name = "numero_destino")
    private String numeroDestino;

    @Column(name = "duracion_segundos")
    private Long duracionSegundos;

    @Column(name = "proveedor")
    private String proveedor;

    @Column(name = "fecha_llamada")
    private LocalDateTime fechaLlamada;

    @Column(name = "fecha_proceso")
    private LocalDateTime fechaProceso;

    // usamos ids simples para no enredar el jpa
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_tipo_llamada")
    private Integer idTipoLlamada;
}
