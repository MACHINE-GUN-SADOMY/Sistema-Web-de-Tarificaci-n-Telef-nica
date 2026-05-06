package cl.anexocontrol.Usuario.Controller.Dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private Long idUsuario;
    private String nombreUsuario;
    private String estadoCuenta;
    private Long idRol;
    private String nombreRol;
}
