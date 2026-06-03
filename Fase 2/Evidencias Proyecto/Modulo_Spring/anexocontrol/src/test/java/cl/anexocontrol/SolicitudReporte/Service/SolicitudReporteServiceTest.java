package cl.anexocontrol.SolicitudReporte.Service;

import cl.anexocontrol.Archivo.Service.ArchivoService;
import cl.anexocontrol.SolicitudReporte.Client.ProcesamientoClient;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesamientoCallbackRequest;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Repository.SolicitudReporteJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudReporteServiceTest {

    @Mock private SolicitudReporteJpaRepository solicitudReporteJpaRepository;
    @Mock private ArchivoService archivoService;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ProcesamientoClient procesamientoClient;

    @InjectMocks private SolicitudReporteService service;

    @Test
    void procesarCallback_LISTO_actualizaEstadoYRuta() {
        SolicitudReporteJpa solicitud = new SolicitudReporteJpa();
        solicitud.setIdSolicitud(1L);
        solicitud.setEstadoSolicitado("PENDIENTE");

        ProcesamientoCallbackRequest request = ProcesamientoCallbackRequest.builder()
                .idSolicitud(1L)
                .estadoSolicitado("LISTO")
                .rutaReporte("/archivos/reportes/reporte_carga_1.pdf")
                .build();

        when(solicitudReporteJpaRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudReporteJpaRepository.save(any(SolicitudReporteJpa.class))).thenReturn(solicitud);

        service.procesarCallback(request);

        assertEquals("LISTO", solicitud.getEstadoSolicitado());
        assertEquals("/archivos/reportes/reporte_carga_1.pdf", solicitud.getRutaReporte());
        verify(solicitudReporteJpaRepository).save(solicitud);
        System.out.println("TEST callback LISTO -> estado: " + solicitud.getEstadoSolicitado() + ", ruta: " + solicitud.getRutaReporte());
    }

    @Test
    void procesarCallback_ERROR_actualizaEstadoError() {
        SolicitudReporteJpa solicitud = new SolicitudReporteJpa();
        solicitud.setIdSolicitud(2L);
        solicitud.setEstadoSolicitado("PENDIENTE");

        ProcesamientoCallbackRequest request = ProcesamientoCallbackRequest.builder()
                .idSolicitud(2L)
                .estadoSolicitado("ERROR")
                .build();

        when(solicitudReporteJpaRepository.findById(2L)).thenReturn(Optional.of(solicitud));
        when(solicitudReporteJpaRepository.save(any(SolicitudReporteJpa.class))).thenReturn(solicitud);

        service.procesarCallback(request);

        assertEquals("ERROR", solicitud.getEstadoSolicitado());
        assertEquals("ERROR", solicitud.getRutaReporte());
        verify(solicitudReporteJpaRepository).save(solicitud);
        System.out.println("TEST callback ERROR -> estado: " + solicitud.getEstadoSolicitado() + ", ruta: " + solicitud.getRutaReporte());
    }

    @Test
    void listarPorUsuarioConPermiso_devuelveOrdenDescendente() {
        SolicitudReporteJpa s1 = new SolicitudReporteJpa();
        s1.setIdSolicitud(1L);
        s1.setFechaSolicitud(LocalDateTime.of(2025, 1, 1, 10, 0));

        SolicitudReporteJpa s2 = new SolicitudReporteJpa();
        s2.setIdSolicitud(2L);
        s2.setFechaSolicitud(LocalDateTime.of(2025, 1, 3, 10, 0)); // más reciente

        SolicitudReporteJpa s3 = new SolicitudReporteJpa();
        s3.setIdSolicitud(3L);
        s3.setFechaSolicitud(LocalDateTime.of(2025, 1, 2, 10, 0));

        when(solicitudReporteJpaRepository.findByIdUsuario(1L)).thenReturn(List.of(s1, s2, s3));

        // idUsuario = idUsuarioSolicitante → acceso como propietario (rol 2 = empleado)
        List<SolicitudReporteJpa> resultado = service.listarPorUsuarioConPermiso(1L, 1L, 2L);

        assertEquals(3, resultado.size());
        assertEquals(2L, resultado.get(0).getIdSolicitud()); // Jan 3 primero
        assertEquals(3L, resultado.get(1).getIdSolicitud()); // Jan 2 segundo
        assertEquals(1L, resultado.get(2).getIdSolicitud()); // Jan 1 último
        System.out.println("TEST orden solicitudes -> total: " + resultado.size() + ", primer id: " + resultado.get(0).getIdSolicitud() + " (mas reciente primero)");
    }
}
