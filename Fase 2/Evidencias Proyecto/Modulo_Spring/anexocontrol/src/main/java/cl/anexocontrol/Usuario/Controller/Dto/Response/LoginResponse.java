package cl.anexocontrol.Usuario.Controller.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long idUsuario;
    private String nombreUsuario;
    private String estadoCuenta;
    private Long idRol;
    private String nombreRol;
    private String mensaje;
}
