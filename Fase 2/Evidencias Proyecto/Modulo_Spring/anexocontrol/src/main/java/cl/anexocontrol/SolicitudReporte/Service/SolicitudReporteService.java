package cl.anexocontrol.SolicitudReporte.Service;

import cl.anexocontrol.Common.Enums.EstadoSolicitudEnum;
import cl.anexocontrol.SolicitudReporte.Client.ProcesamientoClient;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ActualizarEstadoSolicitudRequest;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesamientoCallbackRequest;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ProcesarArchivoRequest;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Repository.SolicitudReporteJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.anexocontrol.Archivo.Service.ArchivoService;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class SolicitudReporteService {
    private final SolicitudReporteJpaRepository solicitudReporteJpaRepository;
    private final ArchivoService archivoService;
    private final JdbcTemplate jdbcTemplate;
    private final ProcesamientoClient procesamientoClient;

    public SolicitudReporteService(
            SolicitudReporteJpaRepository solicitudReporteJpaRepository, ArchivoService archivoService,
            JdbcTemplate jdbcTemplate, ProcesamientoClient procesamientoClient) {
        this.solicitudReporteJpaRepository = solicitudReporteJpaRepository;
        this.archivoService = archivoService;
        this.jdbcTemplate = jdbcTemplate;
        this.procesamientoClient = procesamientoClient;
    }

    // flujo principal: archivo -> solicitud -> django -> listo o error.
    public SolicitudReporteJpa solicitarReporte(MultipartFile archivo, Long idUsuario, Integer idTipoReporte) {
        // se valida lo minimo antes de crear la carga.
        if (idUsuario == null) {
            throw new RuntimeException("El id de usuario es obligatorio");
        }

        if (idTipoReporte == null) {
            throw new RuntimeException("El tipo de reporte es obligatorio");
        }

        // la carga agrupa el archivo y los registros que saldran de el.
        Long idCarga = generarIdCarga();

        // el archivo queda guardado antes de avisarle a django donde leerlo.
        String rutaArchivo = archivoService.guardarArchivo(archivo, idCarga);

        // la solicitud parte pendiente mientras django procesa el archivo.
        SolicitudReporteJpa solicitud = new SolicitudReporteJpa();
        solicitud.setIdCarga(idCarga);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setEstadoSolicitado(EstadoSolicitudEnum.PENDIENTE.name());
        solicitud.setRutaReporte("PENDIENTE");
        solicitud.setIdUsuario(idUsuario);
        solicitud.setIdTipoReporte(idTipoReporte);

        // se guarda primero para mandar a django el id real de solicitud.
        SolicitudReporteJpa solicitudGuardada = solicitudReporteJpaRepository.save(solicitud);

        // este request conecta spring con el modulo django de procesamiento.
        ProcesarArchivoRequest procesarArchivoRequest = ProcesarArchivoRequest.builder()
                .idSolicitud(solicitudGuardada.getIdSolicitud())
                .idCarga(solicitudGuardada.getIdCarga())
                .idUsuario(solicitudGuardada.getIdUsuario())
                .idTipoReporte(solicitudGuardada.getIdTipoReporte())
                .rutaArchivo(rutaArchivo)
                .build();

        // django procesa el archivo; si responde bien dejamos la solicitud lista.
        try {
            procesamientoClient.notificarArchivoListo(procesarArchivoRequest);

            String rutaReporte = construirRutaReporte(rutaArchivo, idCarga, idTipoReporte);

            solicitudGuardada.setEstadoSolicitado(EstadoSolicitudEnum.LISTO.name());
            solicitudGuardada.setRutaReporte(rutaReporte);

            return solicitudReporteJpaRepository.save(solicitudGuardada);
        } catch (Exception exception) {
            // si django falla, dejamos la solicitud marcada como error para la vista.
            solicitudGuardada.setEstadoSolicitado(EstadoSolicitudEnum.ERROR.name());
            solicitudGuardada.setRutaReporte("ERROR");

            return solicitudReporteJpaRepository.save(solicitudGuardada);
        }
    }

    // helper para generar un id de carga usando una secuencia de oracle
    private Long generarIdCarga() {
        return jdbcTemplate.queryForObject(
                "SELECT seq_carga.NEXTVAL FROM dual", Long.class);
    }

    // obtener solicitud por su id en modo solo lectura
    @Transactional(readOnly = true)
    public SolicitudReporteJpa obtenerPorId(Long idSolicitud) {
        if (idSolicitud == null) {
            throw new RuntimeException("El id de solicitud es obligatorio");
        }

        // buscamos por id o lanzamos excepcion si no la pillamos
        return solicitudReporteJpaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud de reporte no encontrada"));
    }

    // obtiene por id pero ademas verifica si el usuario tiene permiso para verla
    @Transactional(readOnly = true)
    public SolicitudReporteJpa obtenerPorIdConPermiso(Long idSolicitud, Long idUsuarioSolicitante,
            Long idRolSolicitante) {
        // usamos el metodo anterior para sacar la solicitud
        SolicitudReporteJpa solicitud = obtenerPorId(idSolicitud);

        // aplicamos la validacion de permisos
        validarAccesoSolicitud(
                solicitud,
                idUsuarioSolicitante,
                idRolSolicitante);

        return solicitud;
    }

    // lista solicitudes de un usuario, dejando las mas nuevas arriba.
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            throw new RuntimeException("El id de usuario es obligatorio");
        }

        return solicitudReporteJpaRepository.findByIdUsuario(idUsuario)
                .stream()
                .sorted((a, b) -> {
                    if (a.getFechaSolicitud() == null && b.getFechaSolicitud() == null) return 0;
                    if (a.getFechaSolicitud() == null) return 1;
                    if (b.getFechaSolicitud() == null) return -1;
                    int cmp = b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                    if (cmp != 0) return cmp;
                    // si tienen la misma fecha, gana el id mas nuevo para mantener orden estable.
                    if (a.getIdSolicitud() == null && b.getIdSolicitud() == null) return 0;
                    if (a.getIdSolicitud() == null) return 1;
                    if (b.getIdSolicitud() == null) return -1;
                    return b.getIdSolicitud().compareTo(a.getIdSolicitud());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // lista solicitudes con permiso, tambien dejando las mas nuevas arriba.
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarPorUsuarioConPermiso(Long idUsuario, Long idUsuarioSolicitante,
            Long idRolSolicitante) {
        // revisamos si el solicitante puede ver la data de este usuario
        validarAccesoUsuario(idUsuario, idUsuarioSolicitante, idRolSolicitante);

        return solicitudReporteJpaRepository.findByIdUsuario(idUsuario)
                .stream()
                .sorted((a, b) -> {
                    if (a.getFechaSolicitud() == null && b.getFechaSolicitud() == null) return 0;
                    if (a.getFechaSolicitud() == null) return 1;
                    if (b.getFechaSolicitud() == null) return -1;
                    int cmp = b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                    if (cmp != 0) return cmp;
                    // si tienen la misma fecha, gana el id mas nuevo para mantener orden estable.
                    if (a.getIdSolicitud() == null && b.getIdSolicitud() == null) return 0;
                    if (a.getIdSolicitud() == null) return 1;
                    if (b.getIdSolicitud() == null) return -1;
                    return b.getIdSolicitud().compareTo(a.getIdSolicitud());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // trae las solicitudes de una carga especifica
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarPorCarga(Long idCarga) {
        if (idCarga == null) {
            throw new RuntimeException("El id de carga es obligatorio");
        }

        return solicitudReporteJpaRepository.findByIdCarga(idCarga);
    }

    // lista por carga pero oculta lo que no sea del usuario si no es admin
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarPorCargaConPermiso(Long idCarga, Long idUsuarioSolicitante,
            Long idRolSolicitante) {
        if (idCarga == null) {
            throw new RuntimeException("El id de carga es obligatorio");
        }

        // sacamos todas las solicitudes vinculadas a la carga
        List<SolicitudReporteJpa> solicitudes = solicitudReporteJpaRepository.findByIdCarga(idCarga);

        // si el que consulta es admin le pasamos todo
        if (esAdministrador(idRolSolicitante)) {
            return solicitudes;
        }

        // si no es admin filtramos para que solo vea sus propias solicitudes
        return solicitudes.stream().filter(solicitud -> solicitud.getIdUsuario().equals(idUsuarioSolicitante)).toList();
    }

    @Transactional
    // actualiza el estado de la solicitud y su ruta
    public SolicitudReporteJpa actualizarEstado(Long idSolicitud, ActualizarEstadoSolicitudRequest request) {
        if (idSolicitud == null) {
            throw new RuntimeException("El id de solicitud es obligatorio");
        }

        if (request.getEstadoSolicitado() == null || request.getEstadoSolicitado().isBlank()) {
            throw new RuntimeException("El estado de solicitud es obligatorio");
        }

        // nos aseguramos que sea un estado valido del enum
        validarEstado(request.getEstadoSolicitado());

        // buscamos la solicitud en base de datos
        SolicitudReporteJpa solicitud = solicitudReporteJpaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud de reporte no encontrada"));

        // le seteamos el nuevo estado
        solicitud.setEstadoSolicitado(request.getEstadoSolicitado());

        // si viene una ruta reporte la cambiamos
        if (request.getRutaReporte() != null && !request.getRutaReporte().isBlank()) {
            solicitud.setRutaReporte(request.getRutaReporte());
        }

        return solicitudReporteJpaRepository.save(solicitud);
    }

    // valida y devuelve la solicitud solo si esta lista para su descarga
    @Transactional(readOnly = true)
    public SolicitudReporteJpa obtenerSolicitudListaParaDescarga(Long idSolicitud) {
        if (idSolicitud == null) {
            throw new RuntimeException("El id de solicitud es obligatorio");
        }
        // la vamos a buscar
        SolicitudReporteJpa solicitud = solicitudReporteJpaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud de reporte no encontrada"));

        // si el estado no es listo tiramos error
        if (!EstadoSolicitudEnum.LISTO.name().equalsIgnoreCase(solicitud.getEstadoSolicitado())) {
            throw new RuntimeException("El reporte aun no esta listo para descarga");
        } // si no hay ruta entonces no hay archivo que descargar
        if (solicitud.getRutaReporte() == null || solicitud.getRutaReporte().isBlank()) {
            throw new RuntimeException("La solicitud no tiene ruta de reporte asociada");
        }
        if (!EstadoSolicitudEnum.LISTO.name().equalsIgnoreCase(solicitud.getEstadoSolicitado())) {
            throw new RuntimeException("El reporte aun no esta listo para descarga");
        }

        return solicitud;
    }

    // igual que el anterior pero limitando la descarga por permisos
    @Transactional(readOnly = true)
    public SolicitudReporteJpa obtenerSolicitudListaParaDescargaConPermiso(Long idSolicitud, Long idUsuarioSolicitante,
            Long idRolSolicitante) {
        // traemos la solicitud que ya paso el filtro de estar lista
        SolicitudReporteJpa solicitud = obtenerSolicitudListaParaDescarga(idSolicitud);

        validarAccesoSolicitud(solicitud, idUsuarioSolicitante, idRolSolicitante);

        return solicitud;
    }

    // comprueba si el texto concuerda con algun valor del enum de estados
    private void validarEstado(String estadoSolicitud) {
        try {
            EstadoSolicitudEnum.valueOf(estadoSolicitud.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de solicitud no valido");
        }
    }

    // logica para validar si un usuario puede acceder a una solicitud especifica
    private void validarAccesoSolicitud(SolicitudReporteJpa solicitud, Long idUsuarioSolicitante,
            Long idRolSolicitante) {
        if (idUsuarioSolicitante == null) {
            throw new RuntimeException("El usuario solicitante es obligatorio");
        }

        if (idRolSolicitante == null) {
            throw new RuntimeException("El rol solicitante es obligatorio");
        }

        // determinamos si es el dueno de la solicitud o un admin
        boolean esAdmin = esAdministrador(idRolSolicitante);
        boolean esPropietario = solicitud.getIdUsuario().equals(idUsuarioSolicitante);

        // si no es ninguna de las dos falla
        if (!esAdmin && !esPropietario) {
            throw new RuntimeException("No tiene permisos para acceder a esta solicitud");
        }
    }

    // logica para validar si alguien puede ver la informacion vinculada a otro id
    // de usuario
    private void validarAccesoUsuario(Long idUsuario, Long idUsuarioSolicitante, Long idRolSolicitante) {
        if (idUsuario == null) {
            throw new RuntimeException("El id de usuario es obligatorio");
        }

        if (idUsuarioSolicitante == null) {
            throw new RuntimeException("El usuario solicitante es obligatorio");
        }

        if (idRolSolicitante == null) {
            throw new RuntimeException("El rol solicitante es obligatorio");
        }

        boolean esAdmin = esAdministrador(idRolSolicitante);
        boolean esPropietario = idUsuario.equals(idUsuarioSolicitante);

        // bloqueamos el paso si esta intentando ver lo ajeno sin ser admin
        if (!esAdmin && !esPropietario) {
            throw new RuntimeException("No tiene permisos para consultar reportes de otro usuario");
        }
    }

    @Transactional
    public SolicitudReporteJpa procesarCallback(ProcesamientoCallbackRequest request) {

        // valida que venga el id de solicitud.
        if (request.getIdSolicitud() == null) {
            throw new RuntimeException("El id de la solicitud no puede ser nulo");
        }

        // valida que venga un estado para aplicar.
        if (request.getEstadoSolicitado() == null || request.getEstadoSolicitado().isBlank()) {
            throw new RuntimeException("El estado de solicitud es obligatorio");
        }

        // normaliza el estado para compararlo sin depender de mayusculas.
        String estado = request.getEstadoSolicitado().trim().toUpperCase();

        // revisa que el estado exista en el enum.
        validarEstado(estado);

        // busca la solicitud que django esta avisando.
        SolicitudReporteJpa solicitud = solicitudReporteJpaRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new RuntimeException("Solicitud de reporte no encontrada"));

        // caso listo: exige ruta porque ya deberia existir el reporte.
        if ("LISTO".equals(estado)) {

            if (request.getRutaReporte() == null || request.getRutaReporte().isBlank()) {
                throw new RuntimeException("La ruta del reporte es obligatoria cuando la solicitud queda LISTO");
            }

            solicitud.setEstadoSolicitado(estado);
            solicitud.setRutaReporte(request.getRutaReporte());
        }

        // caso error: deja una marca simple para la vista.
        else if ("ERROR".equals(estado)) {
            solicitud.setEstadoSolicitado(estado);
            solicitud.setRutaReporte("ERROR");
        }

        // otros estados validos se guardan sin forzar ruta.
        else {
            solicitud.setEstadoSolicitado(estado);

            if (request.getRutaReporte() != null && !request.getRutaReporte().isBlank()) {
                solicitud.setRutaReporte(request.getRutaReporte());
            }
        }

        return solicitudReporteJpaRepository.save(solicitud);
    }

    // trae todo el sistema para el dashboard admin.
    // el orden ayuda a mostrar primero las solicitudes mas nuevas.
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarTodasLasSolicitudes() {
        return solicitudReporteJpaRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    if (a.getFechaSolicitud() == null && b.getFechaSolicitud() == null) return 0;
                    if (a.getFechaSolicitud() == null) return 1;
                    if (b.getFechaSolicitud() == null) return -1;
                    return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // helper simple para saber si el rol corresponde a admin.
    private boolean esAdministrador(Long idRolSolicitante) {
        return idRolSolicitante != null && idRolSolicitante.equals(1L);
    }

    private String construirRutaReporte(String rutaArchivo, Long idCarga, Integer idTipoReporte) {
        String extension;

        if (idTipoReporte == 1) {
            extension = "pdf";
        } else if (idTipoReporte == 2) {
            extension = "csv";
        } else {
            throw new RuntimeException("Tipo de reporte no valido");
        }

        Path rutaArchivoPath = Paths.get(rutaArchivo);

        Path carpetaArchivos = rutaArchivoPath
                .getParent()   // pendientes
                .getParent();  // archivos

        return carpetaArchivos
                .resolve("reportes")
                .resolve("reporte_carga_" + idCarga + "." + extension)
                .toString();
    }

}




