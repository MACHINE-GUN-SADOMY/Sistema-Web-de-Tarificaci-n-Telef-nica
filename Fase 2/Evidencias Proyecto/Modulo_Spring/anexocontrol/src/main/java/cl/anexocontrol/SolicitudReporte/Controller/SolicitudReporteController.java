package cl.anexocontrol.SolicitudReporte.Controller;

import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ActualizarEstadoSolicitudRequest;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Response.SolicitudReporteResponse;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/solicitud-reporte")
public class SolicitudReporteController {
    private final SolicitudReporteService solicitudReporteService;

    public SolicitudReporteController(SolicitudReporteService solicitudReporteService) {
        this.solicitudReporteService = solicitudReporteService;
    }

    // endpoint para crear una solicitud de reporte recibiendo el archivo
    @PostMapping(value = "/crear-solicitud", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> solicitarReporte(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam("idTipoReporte") Integer idTipoReporte) {
        try {
            SolicitudReporteJpa solicitud = solicitudReporteService.solicitarReporte(
                    archivo,
                    idUsuario,
                    idTipoReporte);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(toResponse(solicitud));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // obtener solicitud especifica verificando permisos
    @GetMapping("/obtener-solicitud/{idSolicitud}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long idSolicitud, @RequestParam Long idUsuarioSolicitante,
            @RequestParam Long idRolSolicitante) {
        try {
            SolicitudReporteJpa solicitud = solicitudReporteService.obtenerPorIdConPermiso(
                    idSolicitud,
                    idUsuarioSolicitante,
                    idRolSolicitante);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(toResponse(solicitud));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // listar solicitudes de un usuario verificando quien las pide
    @GetMapping("/listar-solicitud-por-usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long idUsuario, @RequestParam Long idUsuarioSolicitante,
            @RequestParam Long idRolSolicitante) {
        try {
            List<SolicitudReporteResponse> solicitudes = solicitudReporteService
                    .listarPorUsuarioConPermiso(
                            idUsuario,
                            idUsuarioSolicitante,
                            idRolSolicitante)
                    .stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(solicitudes);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // listar solicitudes de una carga especifica filtrando por permisos
    @GetMapping("/listar-solicitud-por-carga/{idCarga}")
    public ResponseEntity<?> listarPorCarga(@PathVariable Long idCarga, @RequestParam Long idUsuarioSolicitante,
            @RequestParam Long idRolSolicitante) {
        try {
            List<SolicitudReporteResponse> solicitudes = solicitudReporteService
                    .listarPorCargaConPermiso(
                            idCarga,
                            idUsuarioSolicitante,
                            idRolSolicitante)
                    .stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(solicitudes);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // endpoint para que el sistema actualice el estado
    @PutMapping("/actualizar-estado-solicitud/{idSolicitud}")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long idSolicitud,
            @RequestBody ActualizarEstadoSolicitudRequest request) {
        try {
            SolicitudReporteJpa solicitud = solicitudReporteService.actualizarEstado(
                    idSolicitud,
                    request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(toResponse(solicitud));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // endpoint para descargar el archivo del reporte si esta listo
    @GetMapping("/{idSolicitud}/descargar")
    public ResponseEntity<?> descargarReporte(@PathVariable Long idSolicitud,
            @RequestParam Long idUsuarioSolicitante, @RequestParam Long idRolSolicitante) {
        try {
            SolicitudReporteJpa solicitud = solicitudReporteService.obtenerSolicitudListaParaDescargaConPermiso(
                    idSolicitud,
                    idUsuarioSolicitante,
                    idRolSolicitante);

            // armamos la ruta del archivo
            Path rutaArchivo = Paths.get(solicitud.getRutaReporte());
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            // revisamos que el archivo exista y se pueda leer
            if (!recurso.exists() || !recurso.isReadable()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("El archivo no existe o no se puede leer");
            }

            // extraemos el nombre del archivo para la descarga
            String nombreArchivo = rutaArchivo.getFileName().toString();

            // enviamos el archivo en la respuesta
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(recurso);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error al descargar el reporte");
        }
    }

    // mapeador manual de entidad a dto
    private SolicitudReporteResponse toResponse(SolicitudReporteJpa solicitud) {
        return SolicitudReporteResponse.builder()
                .idSolicitud(solicitud.getIdSolicitud())
                .idCarga(solicitud.getIdCarga())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .estadoSolicitado(solicitud.getEstadoSolicitado())
                .rutaReporte(solicitud.getRutaReporte())
                .idUsuario(solicitud.getIdUsuario())
                .idTipoReporte(solicitud.getIdTipoReporte())
                .build();
    }
}
