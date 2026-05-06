package cl.anexocontrol.Usuario.Controller;

import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarMiCuentaRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarUsuarioAdminRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.CrearUsuarioRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.EliminarUsuarioRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.LoginRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Response.LoginResponse;
import cl.anexocontrol.Usuario.Controller.Dto.Response.UsuarioResponse;
import cl.anexocontrol.Usuario.Repository.Jpa.UsuarioJpa;
import cl.anexocontrol.Usuario.Service.UsuarioService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/mostrar-usuarios")
    public ResponseEntity<?> obtenerTodosLosUsuarios(
            @RequestParam(name = "idRolSolicitante") Long idRolSolicitante) {
        try {
            return ResponseEntity.ok(
                    usuarioService.mostrarTodosLosUsuarios(idRolSolicitante)
                            .stream()
                            .map(this::toResponse)
                            .toList());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @GetMapping("/mostrar-usuarios/{idUsuario}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long idUsuario) {
        try {
            UsuarioJpa usuario = usuarioService.obtenerUsuarioPorId(idUsuario);
            return ResponseEntity.ok(toResponse(usuario));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/registrar-usuario")
    public ResponseEntity<?> registrarUsuario(@RequestBody CrearUsuarioRequest request) {
        try {
            UsuarioJpa usuarioRegistrado = usuarioService.registrarUsuario(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(toResponse(usuarioRegistrado));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/actualizar-cuenta/{idUsuario}")
    public ResponseEntity<?> modificarMiCuenta(
            @PathVariable Long idUsuario,
            @RequestBody ActualizarMiCuentaRequest request) {
        try {
            UsuarioJpa usuarioActualizado = usuarioService.modificarMiCuenta(idUsuario, request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(toResponse(usuarioActualizado));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/modificar-usuario/{idUsuario}")
    public ResponseEntity<?> modificarUsuarioAdmin(
            @PathVariable Long idUsuario,
            @RequestBody ActualizarUsuarioAdminRequest request) {
        try {
            UsuarioJpa usuarioActualizado = usuarioService.modificarUsuarioAdmin(idUsuario, request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(toResponse(usuarioActualizado));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/eliminar-usuario/{idUsuario}")
    public ResponseEntity<?> eliminarUsuarioAdmin(
            @PathVariable Long idUsuario,
            @RequestBody EliminarUsuarioRequest request) {
        try {
            usuarioService.eliminarUsuarioAdmin(idUsuario, request.getIdRolSolicitante());

            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // endpoint para que los usuarios inicien sesion
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // llamamos al servicio para validar los datos
            LoginResponse response = usuarioService.login(request);

            // si todo sale bien devolvemos un 200 con la info
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        } catch (RuntimeException e) {
            // si falla por clave mala o algo devolvemos el error
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Builder DTO
    private UsuarioResponse toResponse(UsuarioJpa usuario) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .estadoCuenta(usuario.getEstadoCuenta())
                .idRol(usuario.getRol() != null ? usuario.getRol().getIdRol() : null)
                .nombreRol(usuario.getRol() != null ? usuario.getRol().getNombreRol() : null)
                .build();
    }
}
