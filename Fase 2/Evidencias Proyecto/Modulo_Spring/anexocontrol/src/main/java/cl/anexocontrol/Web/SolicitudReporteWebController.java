package cl.anexocontrol.Web;

import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Service.SolicitudReporteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/web")
public class SolicitudReporteWebController {

    private final SolicitudReporteService solicitudReporteService;

    public SolicitudReporteWebController(SolicitudReporteService solicitudReporteService) {
        this.solicitudReporteService = solicitudReporteService;
    }

    // este controller se usa para formularios html, no para api json.
    // /web/crear-solicitud recibe el form visual y vuelve a /reportes.
    @PostMapping("/crear-solicitud")
    public String crearSolicitud(@RequestParam MultipartFile archivo,
                                  @RequestParam Long idUsuario,
                                  @RequestParam Integer idTipoReporte,
                                  RedirectAttributes redirectAttributes) {
        try {
            solicitudReporteService.solicitarReporte(archivo, idUsuario, idTipoReporte);
            redirectAttributes.addFlashAttribute("msg", "Solicitud creada. En procesamiento.");
            return "redirect:/reportes";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reportes";
        }
    }

    // descarga desde el flujo web y usa la sesion para validar permisos.
    // si algo falla, manda a una pantalla visual de error.
    @GetMapping("/descargar/{idSolicitud}")
    public void descargar(@PathVariable Long idSolicitud,
                           HttpSession session,
                           HttpServletResponse response) throws IOException {

        Long idUsuario        = (Long) session.getAttribute("idUsuario");
        Long idRolSolicitante = (Long) session.getAttribute("idRolSolicitante");

        try {
            SolicitudReporteJpa solicitud = solicitudReporteService
                    .obtenerSolicitudListaParaDescargaConPermiso(idSolicitud, idUsuario, idRolSolicitante);

            Path rutaArchivo = Paths.get(solicitud.getRutaReporte());
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                response.sendRedirect("/error/reporte-no-disponible");
                return;
            }

            String nombreArchivo = rutaArchivo.getFileName().toString();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + nombreArchivo + "\"");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            try (InputStream is = recurso.getInputStream()) {
                is.transferTo(response.getOutputStream());
            }

        } catch (Exception e) {
            response.sendRedirect("/error/reporte-no-disponible");
        }
    }
}

