package cl.anexocontrol.SolicitudReporte.Controller;

import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesamientoCallbackRequest;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// pruebas del endpoint POST /procesamiento/callback
// este endpoint recibe avisos de django cuando termina de procesar un archivo
// se mockea el service para no tocar oracle ni llamar a django real
// se verifica que el controller delegue correctamente y responda HTTP 200
@ExtendWith(MockitoExtension.class)
class ProcesamientoCallbackControllerTest {

    @Mock private SolicitudReporteService solicitudReporteService;
    @InjectMocks private ProcesamientoCallbackController controller;

    // simula un callback de django con estado LISTO y ruta del reporte
    // el service mockado devuelve la solicitud ya actualizada
    // el assert verifica que la respuesta del controller sea HTTP 200
    // el verify confirma que el controller llamo a procesarCallback con el request correcto
    @Test
    void recibirCallbackLISTO_delegaEnService() {
        ProcesamientoCallbackRequest request = ProcesamientoCallbackRequest.builder()
                .idSolicitud(1L)
                .estadoSolicitado("LISTO")
                .rutaReporte("/archivos/reportes/reporte_carga_1.pdf")
                .build();

        // solicitud en estado final que el service mockado va a retornar
        SolicitudReporteJpa solicitud = new SolicitudReporteJpa();
        solicitud.setIdSolicitud(1L);
        solicitud.setEstadoSolicitado("LISTO");
        solicitud.setRutaReporte("/archivos/reportes/reporte_carga_1.pdf");

        when(solicitudReporteService.procesarCallback(request)).thenReturn(solicitud);

        ResponseEntity<?> response = controller.recibirCallback(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(solicitudReporteService).procesarCallback(request);
        System.out.println("TEST callback controller LISTO -> HTTP: " + response.getStatusCode() + ", estado solicitud: " + solicitud.getEstadoSolicitado());
    }

    // simula un callback de django con estado ERROR, sin ruta de reporte
    // el controller igual debe responder HTTP 200 y delegar al service
    // el service es quien decide que hacer con el estado ERROR en la solicitud
    @Test
    void recibirCallbackERROR_delegaEnService() {
        ProcesamientoCallbackRequest request = ProcesamientoCallbackRequest.builder()
                .idSolicitud(2L)
                .estadoSolicitado("ERROR")
                .build();

        // solicitud con estado ERROR y marca de ruta como "ERROR"
        SolicitudReporteJpa solicitud = new SolicitudReporteJpa();
        solicitud.setIdSolicitud(2L);
        solicitud.setEstadoSolicitado("ERROR");
        solicitud.setRutaReporte("ERROR");

        when(solicitudReporteService.procesarCallback(request)).thenReturn(solicitud);

        ResponseEntity<?> response = controller.recibirCallback(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(solicitudReporteService).procesarCallback(request);
        System.out.println("TEST callback controller ERROR -> HTTP: " + response.getStatusCode() + ", estado solicitud: " + solicitud.getEstadoSolicitado());
    }
}
