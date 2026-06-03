package cl.anexocontrol.Web;

import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import cl.anexocontrol.Usuario.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    @Mock private UsuarioService usuarioService;
    @Mock private SolicitudReporteService solicitudReporteService;
    @Mock private HttpSession session;
    @Mock private Model model;

    @InjectMocks private ViewController viewController;

    @Test
    void dashboard_sinSesion_redirigeALogin() {
        // sin stubs: session devuelve null en todos los getAttribute → redirige
        String resultado = viewController.dashboard(model, session);
        assertEquals("redirect:/login", resultado);
        System.out.println("TEST sin sesion -> redirect esperado: redirect:/login, obtenido: " + resultado);
    }

    @Test
    void dashboard_admin_retornaAdminDashboard() {
        when(session.getAttribute("idUsuario")).thenReturn(1L);
        when(session.getAttribute("idRolSolicitante")).thenReturn(1L);
        when(solicitudReporteService.listarTodasLasSolicitudes())
                .thenReturn(Collections.emptyList());
        when(usuarioService.mostrarTodosLosUsuarios(1L))
                .thenReturn(Collections.emptyList());
        when(solicitudReporteService.listarPorUsuarioConPermiso(1L, 1L, 1L))
                .thenReturn(Collections.emptyList());

        String resultado = viewController.dashboard(model, session);

        assertEquals("admin-dashboard", resultado);
        System.out.println("TEST dashboard admin -> vista esperada: admin-dashboard, obtenida: " + resultado);
    }

    @Test
    void dashboard_empleado_retornaEmpleadoDashboard() {
        when(session.getAttribute("idUsuario")).thenReturn(5L);
        when(session.getAttribute("idRolSolicitante")).thenReturn(2L);
        when(solicitudReporteService.listarPorUsuarioConPermiso(5L, 5L, 2L))
                .thenReturn(Collections.emptyList());

        String resultado = viewController.dashboard(model, session);

        assertEquals("empleado-dashboard", resultado);
        System.out.println("TEST dashboard empleado -> vista esperada: empleado-dashboard, obtenida: " + resultado);
    }

    @Test
    void solicitudReportes_conSesion_retornaVistaReportes() {
        when(session.getAttribute("idUsuario")).thenReturn(3L);
        when(session.getAttribute("idRolSolicitante")).thenReturn(1L);
        when(solicitudReporteService.listarPorUsuarioConPermiso(3L, 3L, 1L))
                .thenReturn(Collections.emptyList());

        String resultado = viewController.solicitudReportes(model, session);

        assertEquals("admin-empleado-solicitud-reportes", resultado);
        System.out.println("TEST solicitud reportes -> vista esperada: admin-empleado-solicitud-reportes, obtenida: " + resultado);
    }
}
