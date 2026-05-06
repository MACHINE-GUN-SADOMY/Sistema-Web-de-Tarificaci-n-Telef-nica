package cl.anexocontrol.Usuario.Controller.Dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarUsuarioAdminRequest {
    private String nombreUsuario;
    private String contrasenha;
    private String estadoCuenta;
    private Long idRol;
    private Long idRolSolicitante;
}
