package cl.anexocontrol.Web;

import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import cl.anexocontrol.Usuario.Controller.Dto.Response.UsuarioResponse;
import cl.anexocontrol.Usuario.Repository.Jpa.UsuarioJpa;
import cl.anexocontrol.Usuario.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    private final UsuarioService usuarioService;
    private final SolicitudReporteService solicitudReporteService;

    public ViewController(UsuarioService usuarioService,
                          SolicitudReporteService solicitudReporteService) {
        this.usuarioService = usuarioService;
        this.solicitudReporteService = solicitudReporteService;
    }

    // --- Helpers ---

    private void addSessionToModel(Model model, HttpSession session) {
        model.addAttribute("idUsuario",        session.getAttribute("idUsuario"));
        model.addAttribute("idRolSolicitante", session.getAttribute("idRolSolicitante"));
        model.addAttribute("nombreUsuario",    session.getAttribute("nombreUsuario"));
        model.addAttribute("rolUsuario",       session.getAttribute("nombreRol"));
    }

    private UsuarioResponse toResponse(UsuarioJpa u) {
        return UsuarioResponse.builder()
                .idUsuario(u.getIdUsuario())
                .nombreUsuario(u.getNombreUsuario())
                .estadoCuenta(u.getEstadoCuenta())
                .idRol(u.getRol() != null ? u.getRol().getIdRol() : null)
                .nombreRol(u.getRol() != null ? u.getRol().getNombreRol() : null)
                .build();
    }

    private void cargarUsuario(Long idUsuario, Model model) {
        try {
            model.addAttribute("usuario",
                    toResponse(usuarioService.obtenerUsuarioPorId(idUsuario)));
        } catch (RuntimeException e) {
            // fallback visual activo
        }
    }

    // --- Vistas públicas ---

    @GetMapping({"/", "/login"})
    public String login() {
        return "empleado-admin-login";
    }

    @GetMapping("/registrar-usuario")
    public String registrarUsuario() {
        return "empleado-admin-registrar-usuario";
    }

    // --- Dashboard ---

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idUsuario        = (Long) session.getAttribute("idUsuario");
        Long idRolSolicitante = (Long) session.getAttribute("idRolSolicitante");

        if (idUsuario != null && idRolSolicitante != null) {
            try {
                List<SolicitudReporteJpa> solicitudes = solicitudReporteService
                        .listarPorUsuarioConPermiso(idUsuario, idUsuario, idRolSolicitante);

                model.addAttribute("totalReportes",
                        solicitudes.stream()
                                .filter(s -> "LISTO".equals(s.getEstadoSolicitado())).count());
                model.addAttribute("solicitudesPendientes",
                        solicitudes.stream()
                                .filter(s -> "PENDIENTE".equals(s.getEstadoSolicitado())).count());
                model.addAttribute("erroresProcesamiento",
                        solicitudes.stream()
                                .filter(s -> "ERROR".equals(s.getEstadoSolicitado())).count());

                model.addAttribute("solicitudes", solicitudes.stream()
                        .sorted((a, b) -> {
                            if (a.getFechaSolicitud() == null
                                    || b.getFechaSolicitud() == null) return 0;
                            return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                        })
                        .limit(10)
                        .collect(Collectors.toList()));
            } catch (RuntimeException e) {
                // fallback visual activo
            }
        }

        if (idRolSolicitante != null) {
            try {
                model.addAttribute("usuariosActivos",
                        usuarioService.mostrarTodosLosUsuarios(idRolSolicitante).size());
            } catch (RuntimeException e) {
                // PENDIENTE: rol sin permiso — fallback visual activo (muestra 0)
            }
        }

        return "admin-dashboard";
    }

    // --- Solicitud de Reportes + Historial ---

    @GetMapping("/reportes")
    public String solicitudReportes(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idUsuario        = (Long) session.getAttribute("idUsuario");
        Long idRolSolicitante = (Long) session.getAttribute("idRolSolicitante");

        if (idUsuario != null && idRolSolicitante != null) {
            try {
                model.addAttribute("solicitudes", solicitudReporteService
                        .listarPorUsuarioConPermiso(idUsuario, idUsuario, idRolSolicitante));
            } catch (RuntimeException e) {
                // fallback visual activo
            }
        }

        return "admin-empleado-solicitud-reportes.html";
    }

    // ELIMINADO: GET /reportes/{idSolicitud}
    // La vista detalle-solicitud fue eliminada del proyecto.
    // "Ver detalle" para solicitudes ERROR apunta directamente a /error/solicitud-con-error.

    // --- Pantallas de error controlado ---

    @GetMapping("/error/solicitud-con-error")
    public String errorSolicitudConError(Model model, HttpSession session) {
        addSessionToModel(model, session);
        model.addAttribute("tituloError",  "Error en la Solicitud");
        model.addAttribute("estadoError",  "ERROR");
        model.addAttribute("mensajeError",
                "La solicitud no pudo ser procesada correctamente.");
        model.addAttribute("urlVolver", "/reportes");
        return "error";
    }

    @GetMapping("/error/reporte-no-disponible")
    public String errorReporteNoDisponible(Model model, HttpSession session) {
        addSessionToModel(model, session);
        model.addAttribute("tituloError",  "Reporte no disponible");
        model.addAttribute("estadoError",  "NO DISPONIBLE");
        model.addAttribute("mensajeError",
                "El reporte solicitado no existe o ya no está disponible.");
        model.addAttribute("urlVolver", "/reportes");
        return "error";
    }

    // --- Usuarios ---

    @GetMapping("/usuarios")
    public String usuarios(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idRolSolicitante = (Long) session.getAttribute("idRolSolicitante");
        if (idRolSolicitante != null) {
            try {
                List<UsuarioResponse> usuarios = usuarioService
                        .mostrarTodosLosUsuarios(idRolSolicitante)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
                model.addAttribute("usuarios", usuarios);
            } catch (RuntimeException e) {
                // fallback visual activo
            }
        }

        return "admin-usuarios";
    }

    @GetMapping("/usuarios/{idUsuario}")
    public String detalleUsuario(@PathVariable Long idUsuario,
                                 Model model, HttpSession session) {
        addSessionToModel(model, session);
        cargarUsuario(idUsuario, model);
        return "admin-modificar-usuario";
    }

    @GetMapping("/usuarios/{idUsuario}/modificar")
    public String modificarUsuario(@PathVariable Long idUsuario,
                                   Model model, HttpSession session) {
        addSessionToModel(model, session);
        cargarUsuario(idUsuario, model);
        return "admin-modificar-usuario";
    }

    // --- Cuenta ---

    @GetMapping("/cuenta")
    public String actualizarCuenta(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idUsuario = (Long) session.getAttribute("idUsuario");
        if (idUsuario != null) {
            try {
                model.addAttribute("usuarioActual",
                        toResponse(usuarioService.obtenerUsuarioPorId(idUsuario)));
            } catch (RuntimeException e) {
                // fallback visual activo
            }
        }

        return "empleado-actualizar-cuenta";
    }

    // --- Logout provisional ---

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
