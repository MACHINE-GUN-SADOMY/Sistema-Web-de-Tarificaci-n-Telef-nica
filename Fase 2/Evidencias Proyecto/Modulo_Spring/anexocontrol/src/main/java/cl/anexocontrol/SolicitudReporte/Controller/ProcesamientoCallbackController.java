package cl.anexocontrol.SolicitudReporte.Controller;

import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesamientoCallbackRequest;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Response.SolicitudReporteResponse;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/procesamiento")
public class ProcesamientoCallbackController {
    // declaramos el objeto service
    private final SolicitudReporteService solicitudReporteService;

    // creamos su constructor
    public ProcesamientoCallbackController(SolicitudReporteService solicitudReporteService) {
        this.solicitudReporteService = solicitudReporteService;
    }

    // creamos el endpoint callback
    @PostMapping("/callback")
    public ResponseEntity<?> recibirCallback(@RequestBody ProcesamientoCallbackRequest request){
        try{
            SolicitudReporteJpa solicitud = solicitudReporteService.procesarCallback(request);

            return ResponseEntity.status(HttpStatus.OK).body(toResponse(solicitud));

        }catch (Exception exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    // el builder response
    private SolicitudReporteResponse toResponse(SolicitudReporteJpa solicitudJpa){
        return SolicitudReporteResponse.builder()
                .idSolicitud(solicitudJpa.getIdSolicitud())
                .idCarga(solicitudJpa.getIdCarga())
                .fechaSolicitud(solicitudJpa.getFechaSolicitud())
                .estadoSolicitado(solicitudJpa.getEstadoSolicitado())
                .rutaReporte(solicitudJpa.getRutaReporte())
                .idUsuario(solicitudJpa.getIdUsuario())
                .idTipoReporte(solicitudJpa.getIdTipoReporte())
                .build();
    }
}
