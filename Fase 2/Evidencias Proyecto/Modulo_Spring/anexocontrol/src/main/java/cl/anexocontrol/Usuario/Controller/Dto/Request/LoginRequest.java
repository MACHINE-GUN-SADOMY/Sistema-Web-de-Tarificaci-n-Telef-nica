package cl.anexocontrol.Usuario.Controller.Dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String nombreUsuario;
    private String contrasenha;
}
