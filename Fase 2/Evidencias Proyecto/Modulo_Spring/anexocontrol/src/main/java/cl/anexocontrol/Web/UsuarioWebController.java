package cl.anexocontrol.Web;

import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarMiCuentaRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarUsuarioAdminRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.CrearUsuarioRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.LoginRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Response.LoginResponse;
import cl.anexocontrol.Usuario.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web")
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    public UsuarioWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // POST /web/login
    // En caso de error redirige a /login?error=true para mostrar mensaje visual con th:if="${param.error}"
    @PostMapping("/login")
    public String login(@RequestParam String nombreUsuario,
                        @RequestParam String contrasenha,
                        HttpSession session) {
        try {
            LoginRequest request = new LoginRequest();
            request.setNombreUsuario(nombreUsuario);
            request.setContrasenha(contrasenha);

            LoginResponse response = usuarioService.login(request);

            session.setAttribute("idUsuario",        response.getIdUsuario());
            session.setAttribute("nombreUsuario",    response.getNombreUsuario());
            session.setAttribute("idRolSolicitante", response.getIdRol());
            session.setAttribute("nombreRol",        response.getNombreRol());

            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            return "redirect:/login?error=true";
        }
    }

    // POST /web/registrar-usuario
    @PostMapping("/registrar-usuario")
    public String registrarUsuario(@RequestParam String nombreUsuario,
                                   @RequestParam String contrasenha,
                                   @RequestParam(defaultValue = "2") Long idRol,
                                   RedirectAttributes redirectAttributes) {
        try {
            CrearUsuarioRequest request = new CrearUsuarioRequest();
            request.setNombreUsuario(nombreUsuario);
            request.setContrasenha(contrasenha);
            request.setEstadoCuenta("ACTIVA");
            request.setIdRol(idRol);

            usuarioService.registrarUsuario(request);
            redirectAttributes.addFlashAttribute("msg", "Usuario registrado. Inicia sesiÃ³n.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registrar-usuario";
        }
    }

    // POST /web/actualizar-cuenta/{idUsuario}
    @PostMapping("/actualizar-cuenta/{idUsuario}")
    public String actualizarCuenta(@PathVariable Long idUsuario,
                                   @RequestParam String nombreUsuario,
                                   @RequestParam String contrasenha,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            ActualizarMiCuentaRequest request = new ActualizarMiCuentaRequest();
            request.setNombreUsuario(nombreUsuario);
            request.setContrasenha(contrasenha);

            usuarioService.modificarMiCuenta(idUsuario, request);
            session.setAttribute("nombreUsuario", nombreUsuario);
            redirectAttributes.addFlashAttribute("msg", "Cuenta actualizada correctamente.");
            return "redirect:/cuenta";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta";
        }
    }

    // POST /web/modificar-usuario/{idUsuario}
    @PostMapping("/modificar-usuario/{idUsuario}")
    public String modificarUsuario(@PathVariable Long idUsuario,
                                   @RequestParam String nombreUsuario,
                                   @RequestParam String contrasenha,
                                   @RequestParam String estadoCuenta,
                                   @RequestParam Long idRol,
                                   @RequestParam Long idRolSolicitante,
                                   RedirectAttributes redirectAttributes) {
        try {
            ActualizarUsuarioAdminRequest request = new ActualizarUsuarioAdminRequest();
            request.setNombreUsuario(nombreUsuario);
            request.setContrasenha(contrasenha);
            request.setEstadoCuenta(estadoCuenta);
            request.setIdRol(idRol);
            request.setIdRolSolicitante(idRolSolicitante);

            usuarioService.modificarUsuarioAdmin(idUsuario, request);
            redirectAttributes.addFlashAttribute("msg", "Usuario modificado correctamente.");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios";
        }
    }
}
