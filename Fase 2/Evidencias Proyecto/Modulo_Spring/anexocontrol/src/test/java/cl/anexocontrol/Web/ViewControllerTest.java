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

// pruebas del routing de vistas en ViewController
// se mockean session, model y los dos services
// no hay contexto spring ni conexion a oracle
@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    @Mock private UsuarioService usuarioService;
    @Mock private SolicitudReporteService solicitudReporteService;
    @Mock private HttpSession session;
    @Mock private Model model;

    @InjectMocks private ViewController viewController;

    // sin stubs en session, todos los getAttribute devuelven null
    // el controller no tiene usuario ni rol, debe redirigir al login
    @Test
    void dashboard_sinSesion_redirigeALogin() {
        String resultado = viewController.dashboard(model, session, null);
        assertEquals("redirect:/login", resultado);
        System.out.println("TEST sin sesion -> redirect esperado: redirect:/login, obtenido: " + resultado);
    }

    // sesion con idUsuario=1 e idRolSolicitante=1 (admin)
    // se mockean los tres llamados al service que hace el dashboard admin
    // el resultado esperado es la vista admin-dashboard
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

        String resultado = viewController.dashboard(model, session, null);

        assertEquals("admin-dashboard", resultado);
        System.out.println("TEST dashboard admin -> vista esperada: admin-dashboard, obtenida: " + resultado);
    }

    // sesion con idRolSolicitante=2 (empleado)
    // el empleado solo carga sus propias solicitudes, no datos globales
    // se mockea solo listarPorUsuarioConPermiso con el id del empleado
    @Test
    void dashboard_empleado_retornaEmpleadoDashboard() {
        when(session.getAttribute("idUsuario")).thenReturn(5L);
        when(session.getAttribute("idRolSolicitante")).thenReturn(2L);
        when(solicitudReporteService.listarPorUsuarioConPermiso(5L, 5L, 2L))
                .thenReturn(Collections.emptyList());

        String resultado = viewController.dashboard(model, session, null);

        assertEquals("empleado-dashboard", resultado);
        System.out.println("TEST dashboard empleado -> vista esperada: empleado-dashboard, obtenida: " + resultado);
    }

    // prueba el GET /reportes con sesion activa
    // se mockea listarPorUsuarioConPermiso para que no falle al cargar las solicitudes
    // el resultado esperado es la vista de solicitud de reportes
    @Test
    void solicitudReportes_conSesion_retornaVistaReportes() {
        when(session.getAttribute("idUsuario")).thenReturn(3L);
        when(session.getAttribute("idRolSolicitante")).thenReturn(1L);
        when(solicitudReporteService.listarPorUsuarioConPermiso(3L, 3L, 1L))
                .thenReturn(Collections.emptyList());

        String resultado = viewController.solicitudReportes(model, session, null);

        assertEquals("admin-empleado-solicitud-reportes", resultado);
        System.out.println("TEST solicitud reportes -> vista esperada: admin-empleado-solicitud-reportes, obtenida: " + resultado);
    }
}
