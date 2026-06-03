package cl.anexocontrol.Usuario.Controller;

import cl.anexocontrol.Rol.Repository.Jpa.RolJpa;
import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarMiCuentaRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarUsuarioAdminRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.CrearUsuarioRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.LoginRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Response.LoginResponse;
import cl.anexocontrol.Usuario.Repository.Jpa.UsuarioJpa;
import cl.anexocontrol.Usuario.Service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// pruebas de los endpoints REST de UsuarioController usando MockMvc
// se mockea el service completo para no conectar a oracle
// standaloneSetup levanta solo el controller sin contexto spring
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // helper que construye un usuario base con rol empleado
    // se reutiliza en todos los tests para no repetir el armado del objeto
    private UsuarioJpa usuarioBase() {
        RolJpa rol = new RolJpa(2L, "Empleado");
        UsuarioJpa u = new UsuarioJpa();
        u.setIdUsuario(1L);
        u.setNombreUsuario("Juan Perez");
        u.setContrasenha("123456");
        u.setEstadoCuenta("ACTIVA");
        u.setRol(rol);
        return u;
    }

    // prueba GET /usuario/mostrar-usuarios con idRolSolicitante como param
    // el service devuelve una lista de un usuario, se espera HTTP 200
    @Test
    void listarUsuarios_delegaAlServiceYRetorna200() throws Exception {
        when(usuarioService.mostrarTodosLosUsuarios(1L)).thenReturn(List.of(usuarioBase()));

        mockMvc.perform(get("/usuario/mostrar-usuarios")
                        .param("idRolSolicitante", "1"))
                .andExpect(status().isOk());

        verify(usuarioService).mostrarTodosLosUsuarios(1L);
        System.out.println("TEST GET /usuario/mostrar-usuarios -> usuarios listados correctamente");
    }

    // prueba GET /usuario/mostrar-usuarios/{id} para obtener un usuario por su id
    // el service devuelve el usuario base, se espera HTTP 200
    @Test
    void obtenerUsuarioPorId_delegaAlServiceYRetorna200() throws Exception {
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuarioBase());

        mockMvc.perform(get("/usuario/mostrar-usuarios/1"))
                .andExpect(status().isOk());

        verify(usuarioService).obtenerUsuarioPorId(1L);
        System.out.println("TEST GET /usuario/mostrar-usuarios/{id} -> usuario obtenido correctamente");
    }

    // prueba POST /usuario/registrar-usuario con un json de creacion
    // el service crea el usuario y retorna el objeto; se espera HTTP 201
    @Test
    void registrarUsuario_delegaAlServiceYRetorna201() throws Exception {
        when(usuarioService.registrarUsuario(any(CrearUsuarioRequest.class))).thenReturn(usuarioBase());

        String json = "{\"nombreUsuario\":\"Juan Perez\",\"contrasenha\":\"123456\",\"estadoCuenta\":\"ACTIVA\",\"idRol\":2}";

        mockMvc.perform(post("/usuario/registrar-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(usuarioService).registrarUsuario(any(CrearUsuarioRequest.class));
        System.out.println("TEST POST /usuario/registrar-usuario -> usuario registrado correctamente");
    }

    // prueba PUT /usuario/actualizar-cuenta/{id} para que un usuario modifique su propia cuenta
    // se usa eq(4L) para asegurar que el id del path llega bien al service
    @Test
    void actualizarCuenta_delegaAlServiceYRetorna200() throws Exception {
        when(usuarioService.modificarMiCuenta(eq(4L), any(ActualizarMiCuentaRequest.class)))
                .thenReturn(usuarioBase());

        String json = "{\"nombreUsuario\":\"Juan Perez Modificado\",\"contrasenha\":\"nueva_clave_123\"}";

        mockMvc.perform(put("/usuario/actualizar-cuenta/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(usuarioService).modificarMiCuenta(eq(4L), any(ActualizarMiCuentaRequest.class));
        System.out.println("TEST PUT /usuario/actualizar-cuenta/{id} -> cuenta actualizada correctamente");
    }

    // prueba PUT /usuario/modificar-usuario/{id} que solo puede ejecutar un admin
    // el json incluye idRolSolicitante para que el service valide el permiso
    @Test
    void modificarUsuario_delegaAlServiceYRetorna200() throws Exception {
        when(usuarioService.modificarUsuarioAdmin(eq(3L), any(ActualizarUsuarioAdminRequest.class)))
                .thenReturn(usuarioBase());

        String json = "{\"nombreUsuario\":\"Admin Editado\",\"contrasenha\":\"admin123\",\"estadoCuenta\":\"BLOQUEADA\",\"idRol\":1,\"idRolSolicitante\":1}";

        mockMvc.perform(put("/usuario/modificar-usuario/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(usuarioService).modificarUsuarioAdmin(eq(3L), any(ActualizarUsuarioAdminRequest.class));
        System.out.println("TEST PUT /usuario/modificar-usuario/{id} -> usuario modificado correctamente");
    }

    // prueba DELETE /usuario/eliminar-usuario/{id} con el idRolSolicitante en el body
    // el service recibe el id del usuario a eliminar y el rol del que pide la accion
    // se espera HTTP 204 sin contenido si la eliminacion fue exitosa
    @Test
    void eliminarUsuario_delegaAlServiceYRetorna204() throws Exception {
        String json = "{\"idRolSolicitante\":1}";

        mockMvc.perform(delete("/usuario/eliminar-usuario/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminarUsuarioAdmin(eq(3L), eq(1L));
        System.out.println("TEST DELETE /usuario/eliminar-usuario/{id} -> usuario eliminado correctamente");
    }

    // prueba POST /usuario/login con credenciales validas
    // el service mockado retorna un LoginResponse con los datos de sesion
    // se espera HTTP 200 si las credenciales son correctas
    @Test
    void login_delegaAlServiceYRetorna200() throws Exception {
        LoginResponse loginResponse = LoginResponse.builder()
                .idUsuario(1L)
                .nombreUsuario("Juan Perez")
                .estadoCuenta("ACTIVA")
                .idRol(2L)
                .nombreRol("Empleado")
                .mensaje("Login correcto")
                .build();

        when(usuarioService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        String json = "{\"nombreUsuario\":\"Juan Perez\",\"contrasenha\":\"123456\"}";

        mockMvc.perform(post("/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(usuarioService).login(any(LoginRequest.class));
        System.out.println("TEST POST /usuario/login -> login delegado correctamente");
    }
}
