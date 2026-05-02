package cl.anexocontrol.Usuario.Repository.Jpa;

import cl.anexocontrol.Rol.Repository.Jpa.RolJpa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lombok para no escribir getters y setters a mano
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioJpa {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "contrasenha")
    private String contrasenha;

    @Column(name = "estado_cuenta")
    private String estadoCuenta;

    // aca se conecta con la tabla de roles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    private RolJpa rol;
}
