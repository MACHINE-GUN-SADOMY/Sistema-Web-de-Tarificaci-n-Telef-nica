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
    // helpers de sesion
    // toma numeros guardados en sesion y los deja como long.
    // asi no falla si el dato viene como integer u otro number.
    private Long sessionLong(HttpSession session, String key) {
        Object val = session.getAttribute(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return null;
    }
    // busca primero idrolsolicitante y despues idrol.
    // se usa porque algunos flujos guardan el rol con nombres distintos.
    private Long resolverRol(HttpSession session) {
        Long rol = sessionLong(session, "idRolSolicitante");
        if (rol == null) {
            rol = sessionLong(session, "idRol");
        }
        return rol;
    }
    // deja los datos de sesion listos para que thymeleaf los use en las vistas.
    // tambien agrega idrol como alias para mantener compatibilidad con templates.
    private void addSessionToModel(Model model, HttpSession session) {
        Long idUsuario        = sessionLong(session, "idUsuario");
        Long idRolSolicitante = resolverRol(session);

        model.addAttribute("idUsuario",        idUsuario);
        model.addAttribute("idRolSolicitante", idRolSolicitante);
        model.addAttribute("idRol",            idRolSolicitante);   // alias para templates antiguos
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
    // vistas publicas

    @GetMapping({"/", "/login"})
    public String login() {
        return "empleado-admin-login";
    }

    @GetMapping("/registrar-usuario")
    public String registrarUsuario() {
        return "empleado-admin-registrar-usuario";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Long idUsuario        = sessionLong(session, "idUsuario");
        Long idRolSolicitante = resolverRol(session);

        // sin sesion valida se vuelve al login.
        if (idUsuario == null || idRolSolicitante == null) {
            return "redirect:/login";
        }

        addSessionToModel(model, session);
        // el admin ve datos globales y propios; el empleado solo datos propios.
        if (Long.valueOf(1L).equals(idRolSolicitante)) {
            cargarDashboardAdmin(model, idUsuario, idRolSolicitante);
            return "admin-dashboard";
        }

        cargarDashboardEmpleado(model, idUsuario, idRolSolicitante);
        return "empleado-dashboard";
    }
    // arma el panel admin mezclando datos del sistema y datos propios.
    // se cargan separados para que la vista no confunda metricas.
    private void cargarDashboardAdmin(Model model, Long idUsuario, Long idRolSolicitante) {
        try {
            List<SolicitudReporteJpa> todas =
                    solicitudReporteService.listarTodasLasSolicitudes();
            // ya viene ordenada desde el service para mostrar lo mas nuevo primero.

            model.addAttribute("totalReportesSistema",
                    todas.stream().filter(s -> "LISTO".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("solicitudesPendientesSistema",
                    todas.stream().filter(s -> "PENDIENTE".equals(s.getEstadoSolicitado())).count());

            model.addAttribute("erroresSistema",
                    todas.stream().filter(s -> "ERROR".equals(s.getEstadoSolicitado())).count());

            // se toman las primeras 10 porque la lista ya viene ordenada.
            model.addAttribute("actividadGlobal",
                    todas.stream().limit(10).collect(Collectors.toList()));

        } catch (RuntimeException e) {
            model.addAttribute("totalReportesSistema", 0);
            model.addAttribute("solicitudesPendientesSistema", 0);
            model.addAttribute("erroresSistema", 0);
            model.addAttribute("actividadGlobal", Collections.emptyList());
        }
        // total de usuarios para el resumen global del admin.
        try {
            model.addAttribute("totalUsuariosSistema",
                    usuarioService.mostrarTodosLosUsuarios(idRolSolicitante).size());
        } catch (RuntimeException e) {
            model.addAttribute("totalUsuariosSistema", 0);
        }
        // si falta sesion, se dejan metricas propias vacias.
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
            // ultimas 5 propias, con las mas nuevas primero.
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
    // arma el panel empleado solo con informacion del usuario autenticado.
    // no carga datos globales porque esta vista es solo de actividad propia.
    private void cargarDashboardEmpleado(Model model, Long idUsuario, Long idRolSolicitante) {
        // si falta sesion, se dejan metricas propias vacias.
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
            // ultimas 10 propias, con las mas nuevas primero.
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
    // solicitud de reportes e historial

    @GetMapping("/reportes")
    public String solicitudReportes(Model model, HttpSession session) {
        addSessionToModel(model, session);

        Long idUsuario        = sessionLong(session, "idUsuario");
        Long idRolSolicitante = resolverRol(session);
        // la pantalla usa estos datos para el formulario y para listar solicitudes propias.
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
    // pantallas de error controlado

    @GetMapping("/error/solicitud-con-error")
    public String errorSolicitudConError(Model model, HttpSession session) {
        // reutiliza el template error para mostrar una salida visual amable.
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
        // mismo template de error, pero con mensaje para descarga no disponible.
        addSessionToModel(model, session);
        model.addAttribute("tituloError",  "Reporte no disponible");
        model.addAttribute("estadoError",  "NO DISPONIBLE");
        model.addAttribute("mensajeError",
                "El reporte solicitado no existe o ya no está disponible.");
        model.addAttribute("urlVolver", "/reportes");
        return "error";
    }
    // usuarios

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
    // cuenta

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
    // logout

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}



