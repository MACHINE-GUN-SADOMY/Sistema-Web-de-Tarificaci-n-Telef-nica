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

@ExtendWith(MockitoExtension.class)
class ProcesamientoCallbackControllerTest {

    @Mock private SolicitudReporteService solicitudReporteService;
    @InjectMocks private ProcesamientoCallbackController controller;

    @Test
    void recibirCallbackLISTO_delegaEnService() {
        ProcesamientoCallbackRequest request = ProcesamientoCallbackRequest.builder()
                .idSolicitud(1L)
                .estadoSolicitado("LISTO")
                .rutaReporte("/archivos/reportes/reporte_carga_1.pdf")
                .build();

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

    @Test
    void recibirCallbackERROR_delegaEnService() {
        ProcesamientoCallbackRequest request = ProcesamientoCallbackRequest.builder()
                .idSolicitud(2L)
                .estadoSolicitado("ERROR")
                .build();

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
