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

import java.util.Collections;
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

    // ─────────────────────────────────────────────
    // Helpers de sesión y entidad
    // ─────────────────────────────────────────────

    /**
     * Extrae un Long de sesion de forma segura.
     * Acepta que el valor este guardado como Long, Integer o cualquier Number
     * (evita ClassCastException si la sesion guardo un Integer).
     */
    private Long sessionLong(HttpSession session, String key) {
        Object val = session.getAttribute(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return null;
    }

    /**
     * Lee idRolSolicitante con fallback a idRol.
     * Algunos flujos del proyecto guardan el rol como "idRol" en lugar de "idRolSolicitante".
     * Se intenta primero "idRolSolicitante"; si viene null se intenta "idRol".
     */
    private Long resolverRol(HttpSession session) {
        Long rol = sessionLong(session, "idRolSolicitante");
        if (rol == null) {
            rol = sessionLong(session, "idRol");
        }
        return rol;
    }

    private void addSessionToModel(Model model, HttpSession session) {
        Long idUsuario        = sessionLong(session, "idUsuario");
        Long idRolSolicitante = resolverRol(session);

        model.addAttribute("idUsuario",        idUsuario);
        model.addAttribute("idRolSolicitante", idRolSolicitante);
        model.addAttribute("idRol",            idRolSolicitante);   // alias por compatibilidad
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

    // ─────────────────────────────────────────────
    // Vistas públicas
    // ─────────────────────────────────────────────

    @GetMapping({"/", "/login"})
    public String login() {
        return "empleado-admin-login";
    }

    @GetMapping("/registrar-usuario")
    public String registrarUsuario() {
        return "empleado-admin-registrar-usuario";
    }

    // ─────────────────────────────────────────────
    // Dashboard — ruta única, template según rol
    // ─────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Long idUsuario        = sessionLong(session, "idUsuario");
        Long idRolSolicitante = resolverRol(session);

        // sin sesion valida, redirigir a login
        if (idUsuario == null || idRolSolicitante == null) {
            return "redirect:/login";
        }

        addSessionToModel(model, session);

        if (Long.valueOf(1L).equals(idRolSolicitante)) {
            cargarDashboardAdmin(model, idUsuario, idRolSolicitante);
            return "admin-dashboard";
        }

        cargarDashboardEmpleado(model, idUsuario, idRolSolicitante);
        return "empleado-dashboard";
    }

    /**
     * Dashboard ADMINISTRADOR
     *
     * Sección 1 — Resumen del Sistema:
     *   totalReportesSistema, solicitudesPendientesSistema, erroresSistema,
     *   totalUsuariosSistema
     *   Fuente: listarTodasLasSolicitudes() → todas las solicitudes, ordenadas DESC
     *
     * Sección 2 — Resumen del Usuario:
     *   misTotalReportes, misSolicitudesPendientes, misErrores,
     *   misUltimasSolicitudes (límite 5, propias del admin autenticado)
     *   Fuente: listarPorUsuarioConPermiso() → solo el usuario autenticado
     *
     * Sección 3 — Actividad Reciente Global:
     *   actividadGlobal (límite 10, todos los estados, todos los usuarios)
     *   Fuente: misma lista del punto 1, ya ordenada
     */
    private void cargarDashboardAdmin(Model model, Long idUsuario, Long idRolSolicitante) {

        // ── Métricas globales + actividad reciente del sistema ─────────────────
        try {
            List<SolicitudReporteJpa> todas =
                    solicitudReporteService.listarTodasLasSolicitudes();
            // ya viene ordenada por fechaSolicitud DESC desde el service

            model.addAttribute("totalReportesSistema",
                    todas.stream().filter(s -> "LISTO".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("solicitudesPendientesSistema",
                    todas.stream().filter(s -> "PENDIENTE".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("erroresSistema",
                    todas.stream().filter(s -> "ERROR".equals(s.getEstadoSolicitado())).count());

            // actividad global: primeras 10 de la lista ya ordenada
            model.addAttribute("actividadGlobal",
                    todas.stream().limit(10).collect(Collectors.toList()));

        } catch (RuntimeException e) {
            model.addAttribute("totalReportesSistema", 0);
            model.addAttribute("solicitudesPendientesSistema", 0);
            model.addAttribute("erroresSistema", 0);
            model.addAttribute("actividadGlobal", Collections.emptyList());
        }

        // ── Total usuarios registrados ─────────────────────────────────────────
        try {
            model.addAttribute("totalUsuariosSistema",
                    usuarioService.mostrarTodosLosUsuarios(idRolSolicitante).size());
        } catch (RuntimeException e) {
            model.addAttribute("totalUsuariosSistema", 0);
        }

        // ── Métricas y solicitudes propias del admin autenticado ───────────────
        if (idUsuario == null || idRolSolicitante == null) {
            model.addAttribute("misTotalReportes", 0);
            model.addAttribute("misSolicitudesPendientes", 0);
            model.addAttribute("misErrores", 0);
            model.addAttribute("misUltimasSolicitudes", Collections.emptyList());
            return;
        }

        try {
            List<SolicitudReporteJpa> mias =
                    solicitudReporteService.listarPorUsuarioConPermiso(
                            idUsuario, idUsuario, idRolSolicitante);

            model.addAttribute("misTotalReportes",
                    mias.stream().filter(s -> "LISTO".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("misSolicitudesPendientes",
                    mias.stream().filter(s -> "PENDIENTE".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("misErrores",
                    mias.stream().filter(s -> "ERROR".equals(s.getEstadoSolicitado())).count());

            // últimas 5 propias, ordenadas por fecha DESC
            List<SolicitudReporteJpa> misUltimas = mias.stream()
                    .sorted((a, b) -> {
                        if (a.getFechaSolicitud() == null && b.getFechaSolicitud() == null) return 0;
                        if (a.getFechaSolicitud() == null) return 1;
                        if (b.getFechaSolicitud() == null) return -1;
                        return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                    })
                    .limit(5)
                    .collect(Collectors.toList());

            model.addAttribute("misUltimasSolicitudes", misUltimas);

        } catch (RuntimeException e) {
            model.addAttribute("misTotalReportes", 0);
            model.addAttribute("misSolicitudesPendientes", 0);
            model.addAttribute("misErrores", 0);
            model.addAttribute("misUltimasSolicitudes", Collections.emptyList());
        }
    }

    /**
     * Dashboard EMPLEADO
     *
     * Solo datos del usuario autenticado. No recibe ni calcula ninguna métrica global.
     *
     * totalReportes, solicitudesPendientes, erroresProcesamiento,
     * solicitudes (últimas 10 propias, todos los estados, ordenadas por fecha DESC)
     */
    private void cargarDashboardEmpleado(Model model, Long idUsuario, Long idRolSolicitante) {
        if (idUsuario == null || idRolSolicitante == null) {
            model.addAttribute("totalReportes", 0);
            model.addAttribute("solicitudesPendientes", 0);
            model.addAttribute("erroresProcesamiento", 0);
            model.addAttribute("solicitudes", Collections.emptyList());
            return;
        }

        try {
            List<SolicitudReporteJpa> mias =
                    solicitudReporteService.listarPorUsuarioConPermiso(
                            idUsuario, idUsuario, idRolSolicitante);

            model.addAttribute("totalReportes",
                    mias.stream().filter(s -> "LISTO".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("solicitudesPendientes",
                    mias.stream().filter(s -> "PENDIENTE".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("erroresProcesamiento",
                    mias.stream().filter(s -> "ERROR".equals(s.getEstadoSolicitado())).count());

            // últimas 10 propias, ordenadas por fecha DESC, todos los estados
            List<SolicitudReporteJpa> ultimas = mias.stream()
                    .sorted((a, b) -> {
                        if (a.getFechaSolicitud() == null && b.getFechaSolicitud() == null) return 0;
                        if (a.getFechaSolicitud() == null) return 1;
                        if (b.getFechaSolicitud() == null) return -1;
                        return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                    })
                    .limit(10)
                    .collect(Collectors.toList());

            model.addAttribute("solicitudes", ultimas);

        } catch (RuntimeException e) {
            model.addAttribute("totalReportes", 0);
            model.addAttribute("solicitudesPendientes", 0);
            model.addAttribute("erroresProcesamiento", 0);
            model.addAttribute("solicitudes", Collections.emptyList());
        }
    }

    // ─────────────────────────────────────────────
    // Solicitud de Reportes + Historial
    // ─────────────────────────────────────────────

    @GetMapping("/reportes")
    public String solicitudReportes(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idUsuario        = sessionLong(session, "idUsuario");
        Long idRolSolicitante = resolverRol(session);

        model.addAttribute("idUsuario", idUsuario);
        model.addAttribute("idUsuarioSolicitante", idUsuario);
        model.addAttribute("idRolSolicitante", idRolSolicitante);

        if (idUsuario != null && idRolSolicitante != null) {
            try {
                model.addAttribute("solicitudes", solicitudReporteService
                        .listarPorUsuarioConPermiso(idUsuario, idUsuario, idRolSolicitante));
            } catch (RuntimeException e) {
                // fallback visual activo
            }
        }

        return "admin-empleado-solicitud-reportes";
    }

    // ─────────────────────────────────────────────
    // Pantallas de error controlado
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    // Usuarios
    // ─────────────────────────────────────────────

    @GetMapping("/usuarios")
    public String usuarios(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idRolSolicitante = resolverRol(session);
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

    // ─────────────────────────────────────────────
    // Cuenta
    // ─────────────────────────────────────────────

    @GetMapping("/cuenta")
    public String actualizarCuenta(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idUsuario = sessionLong(session, "idUsuario");
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

    // ─────────────────────────────────────────────
    // Logout provisional
    // ─────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
