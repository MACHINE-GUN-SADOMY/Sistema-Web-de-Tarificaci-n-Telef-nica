package cl.anexocontrol.SolicitudReporte.Service;

import cl.anexocontrol.Common.Enums.EstadoSolicitudEnum;
import cl.anexocontrol.SolicitudReporte.Controller.Dto.Request.ActualizarEstadoSolicitudRequest;
import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import cl.anexocontrol.SolicitudReporte.Repository.SolicitudReporteJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.anexocontrol.Archivo.Service.ArchivoService;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@Transactional
@Service
public class SolicitudReporteService {
    private final SolicitudReporteJpaRepository solicitudReporteJpaRepository;
    private final ArchivoService archivoService;
    private final JdbcTemplate jdbcTemplate;

    public SolicitudReporteService(
            SolicitudReporteJpaRepository solicitudReporteJpaRepository, ArchivoService archivoService,
            JdbcTemplate jdbcTemplate) {
        this.solicitudReporteJpaRepository = solicitudReporteJpaRepository;
        this.archivoService = archivoService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // metodo para crear una nueva solicitud en base a un request
    // metodo para crear una nueva solicitud vinculada a un archivo
    public SolicitudReporteJpa solicitarReporte(MultipartFile archivo, Long idUsuario, Integer idTipoReporte) {
        // verificamos que el usuario no sea nulo
        if (idUsuario == null) {
            throw new RuntimeException("El id de usuario es obligatorio");
        }

        // exigimos el tipo de reporte
        if (idTipoReporte == null) {
            throw new RuntimeException("El tipo de reporte es obligatorio");
        }

        // creamos un id unico para la carga
        Long idCarga = generarIdCarga();

        // le pasamos el archivo al servicio correspondiente
        archivoService.guardarArchivo(archivo, idCarga);

        // instanciamos la solicitud y seteamos los datos
        SolicitudReporteJpa solicitud = new SolicitudReporteJpa();
        solicitud.setIdCarga(idCarga);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setEstadoSolicitado(EstadoSolicitudEnum.PENDIENTE.name());
        solicitud.setRutaReporte("PENDIENTE");
        solicitud.setIdUsuario(idUsuario);
        solicitud.setIdTipoReporte(idTipoReporte);

        // guardamos la nueva solicitud y la retornamos
        return solicitudReporteJpaRepository.save(solicitud);
    }

    // helper para generar un id de carga usando una secuencia de Oracle
    private Long generarIdCarga() {
        return jdbcTemplate.queryForObject(
                "SELECT seq_carga.NEXTVAL FROM dual",
                Long.class);
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

    // lista todas las solicitudes de un usuario sin importar permisos
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            throw new RuntimeException("El id de usuario es obligatorio");
        }

        return solicitudReporteJpaRepository.findByIdUsuario(idUsuario);
    }

    // lista las solicitudes de un usuario verificando quien las pide
    @Transactional(readOnly = true)
    public List<SolicitudReporteJpa> listarPorUsuarioConPermiso(Long idUsuario, Long idUsuarioSolicitante,
            Long idRolSolicitante) {
        // revisamos si el solicitante puede ver la data de este usuario
        validarAccesoUsuario(idUsuario, idUsuarioSolicitante, idRolSolicitante);

        return solicitudReporteJpaRepository.findByIdUsuario(idUsuario);
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

    // un simple helper para ver si el rol es uno o sea administrador
    private boolean esAdministrador(Long idRolSolicitante) {
        return idRolSolicitante != null && idRolSolicitante.equals(1L);
    }
}
