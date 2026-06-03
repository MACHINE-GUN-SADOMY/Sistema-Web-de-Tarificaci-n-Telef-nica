package cl.anexocontrol.SolicitudReporte.Controller;

import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ActualizarEstadoSolicitudRequest;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// pruebas de los endpoints REST de SolicitudReporteController usando MockMvc
// se mockea el service completo, no hay oracle ni django real
// standaloneSetup levanta solo el controller sin contexto spring completo
@ExtendWith(MockitoExtension.class)
class SolicitudReporteControllerTest {

    @Mock
    private SolicitudReporteService solicitudReporteService;

    @InjectMocks
    private SolicitudReporteController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // helper para construir una solicitud base en estado PENDIENTE
    // se reutiliza en varios tests para no repetir el armado del objeto
    private SolicitudReporteJpa solicitudBase() {
        SolicitudReporteJpa s = new SolicitudReporteJpa();
        s.setIdSolicitud(1L);
        s.setIdCarga(1001L);
        s.setFechaSolicitud(null);
        s.setEstadoSolicitado("PENDIENTE");
        s.setRutaReporte("PENDIENTE");
        s.setIdUsuario(1L);
        s.setIdTipoReporte(1);
        return s;
    }

    // prueba el endpoint POST /solicitud-reporte/crear-solicitud con un archivo multipart
    // se arma un archivo falso en memoria, no se guarda nada en disco ni se llama a django
    // el verify confirma que el controller delego la creacion al service
    @Test
    void crearSolicitud_multipart_delegaAlServiceYRetorna201() throws Exception {
        SolicitudReporteJpa solicitud = solicitudBase();

        when(solicitudReporteService.solicitarReporte(any(), eq(1L), eq(1)))
                .thenReturn(solicitud);

        // archivo en memoria, sin contenido real de llamadas
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "llamadas.txt", MediaType.TEXT_PLAIN_VALUE, "datos".getBytes());

        mockMvc.perform(multipart("/solicitud-reporte/crear-solicitud")
                        .file(archivo)
                        .param("idUsuario", "1")
                        .param("idTipoReporte", "1"))
                .andExpect(status().isCreated());

        verify(solicitudReporteService).solicitarReporte(any(), eq(1L), eq(1));
        System.out.println("TEST POST /solicitud-reporte/crear-solicitud -> solicitud multipart delegada correctamente");
    }

    // prueba el endpoint GET /solicitud-reporte/obtener-solicitud/{id}
    // se mockea obtenerPorIdConPermiso para devolver una solicitud base
    // el verify confirma que el controller paso los parametros correctos al service
    @Test
    void obtenerSolicitudPorId_delegaAlServiceYRetorna200() throws Exception {
        when(solicitudReporteService.obtenerPorIdConPermiso(1L, 1L, 1L))
                .thenReturn(solicitudBase());

        mockMvc.perform(get("/solicitud-reporte/obtener-solicitud/1")
                        .param("idUsuarioSolicitante", "1")
                        .param("idRolSolicitante", "1"))
                .andExpect(status().isOk());

        verify(solicitudReporteService).obtenerPorIdConPermiso(1L, 1L, 1L);
        System.out.println("TEST GET /solicitud-reporte/obtener-solicitud/{id} -> solicitud obtenida correctamente");
    }

    // prueba el endpoint GET /solicitud-reporte/listar-solicitud-por-usuario/{idUsuario}
    // el service devuelve una lista de un elemento, se verifica que el controller retorne 200
    @Test
    void listarSolicitudesPorUsuario_delegaAlServiceYRetorna200() throws Exception {
        when(solicitudReporteService.listarPorUsuarioConPermiso(1L, 1L, 1L))
                .thenReturn(List.of(solicitudBase()));

        mockMvc.perform(get("/solicitud-reporte/listar-solicitud-por-usuario/1")
                        .param("idUsuarioSolicitante", "1")
                        .param("idRolSolicitante", "1"))
                .andExpect(status().isOk());

        verify(solicitudReporteService).listarPorUsuarioConPermiso(1L, 1L, 1L);
        System.out.println("TEST GET /solicitud-reporte/listar-solicitud-por-usuario/{idUsuario} -> lista delegada correctamente");
    }

    // prueba el endpoint GET /solicitud-reporte/listar-solicitud-por-carga/{idCarga}
    // se usa un idCarga distinto al del usuario para validar que el path variable llega bien
    @Test
    void listarSolicitudesPorCarga_delegaAlServiceYRetorna200() throws Exception {
        when(solicitudReporteService.listarPorCargaConPermiso(1002L, 2L, 2L))
                .thenReturn(List.of(solicitudBase()));

        mockMvc.perform(get("/solicitud-reporte/listar-solicitud-por-carga/1002")
                        .param("idUsuarioSolicitante", "2")
                        .param("idRolSolicitante", "2"))
                .andExpect(status().isOk());

        verify(solicitudReporteService).listarPorCargaConPermiso(1002L, 2L, 2L);
        System.out.println("TEST GET /solicitud-reporte/listar-solicitud-por-carga/{idCarga} -> lista por carga delegada correctamente");
    }

    // prueba el endpoint PUT /solicitud-reporte/actualizar-estado-solicitud/{id}
    // se envia un json con estado LISTO y ruta del reporte
    // el verify usa any() en el request porque MockMvc construye el objeto por deserializacion
    @Test
    void actualizarEstadoSolicitud_delegaAlServiceYRetorna200() throws Exception {
        SolicitudReporteJpa solicitud = solicitudBase();
        solicitud.setEstadoSolicitado("LISTO");
        solicitud.setRutaReporte("archivos/reportes/reporte_listo.txt");

        when(solicitudReporteService.actualizarEstado(eq(1L), any(ActualizarEstadoSolicitudRequest.class)))
                .thenReturn(solicitud);

        String json = "{\"estadoSolicitado\":\"LISTO\",\"rutaReporte\":\"archivos/reportes/reporte_listo.txt\"}";

        mockMvc.perform(put("/solicitud-reporte/actualizar-estado-solicitud/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(solicitudReporteService).actualizarEstado(eq(1L), any(ActualizarEstadoSolicitudRequest.class));
        System.out.println("TEST PUT /solicitud-reporte/actualizar-estado-solicitud/{id} -> estado actualizado");
    }

    // prueba el endpoint GET /solicitud-reporte/{id}/descargar cuando el archivo no existe en disco
    // el service devuelve la solicitud con ruta valida pero el archivo fisico no esta
    // el controller debe retornar 400 porque UrlResource no encuentra el archivo
    // no se necesita un archivo real, la ruta ficticia hace fallar el exists() del recurso
    @Test
    void descargarReporte_archivoNoExiste_retorna400() throws Exception {
        SolicitudReporteJpa solicitud = solicitudBase();
        solicitud.setEstadoSolicitado("LISTO");
        solicitud.setRutaReporte("archivos/reportes/no_existe.pdf");

        when(solicitudReporteService.obtenerSolicitudListaParaDescargaConPermiso(1L, 1L, 1L))
                .thenReturn(solicitud);

        mockMvc.perform(get("/solicitud-reporte/1/descargar")
                        .param("idUsuarioSolicitante", "1")
                        .param("idRolSolicitante", "1"))
                .andExpect(status().isBadRequest());

        verify(solicitudReporteService).obtenerSolicitudListaParaDescargaConPermiso(1L, 1L, 1L);
        System.out.println("TEST GET /solicitud-reporte/{id}/descargar -> archivo no existente retorna 400 (acotado, sin archivo real)");
    }
}
