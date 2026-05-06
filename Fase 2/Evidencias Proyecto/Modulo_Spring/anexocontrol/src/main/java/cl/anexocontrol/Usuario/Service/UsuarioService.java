package cl.anexocontrol.Usuario.Service;

import cl.anexocontrol.Rol.Repository.Jpa.RolJpa;
import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarMiCuentaRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.ActualizarUsuarioAdminRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.CrearUsuarioRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Request.LoginRequest;
import cl.anexocontrol.Usuario.Controller.Dto.Response.LoginResponse;
import cl.anexocontrol.Usuario.Repository.Jpa.UsuarioJpa;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import cl.anexocontrol.Rol.Repository.RolJpaRepository;
import cl.anexocontrol.Usuario.Repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final RolJpaRepository rolJpaRepository;

    public UsuarioService(UsuarioJpaRepository usuarioJpaRepository, RolJpaRepository rolJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.rolJpaRepository = rolJpaRepository;
    }

    @Transactional
    public UsuarioJpa obtenerUsuarioPorId(Long idUsuario) {
        return usuarioJpaRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario No encontrado"));
    }

    @Transactional
    public List<UsuarioJpa> mostrarTodosLosUsuarios(Long idRolSolicitante) {
        if (idRolSolicitante == null || idRolSolicitante != 1L) {
            throw new RuntimeException("No tiene permisos para listar usuarios");
        }
        return usuarioJpaRepository.findAll();
    }

    public UsuarioJpa registrarUsuario(CrearUsuarioRequest request) {
        // Validaciones basicas
        if (request.getNombreUsuario() == null | request.getNombreUsuario().isBlank()) {
            throw new RuntimeException("El nombre de usuario es obligatorio");
        }
        if (request.getContrasenha() == null || request.getContrasenha().isBlank()) {
            throw new RuntimeException("La contrasenha es obligatorio");
        }
        if (request.getIdRol() == null) {
            throw new RuntimeException("El rol es obligatorio");
        }
        if (usuarioJpaRepository.existsByNombreUsuarioIgnoreCase(request.getNombreUsuario())) {
            throw new RuntimeException("El rol es obligatorio");
        }
        // Aqui usaremos la funcion de JpaRepository
        RolJpa rol = rolJpaRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        UsuarioJpa usuario = new UsuarioJpa();
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setContrasenha(request.getContrasenha());
        // importante, aqui tenemos que setear el estado
        usuario.setEstadoCuenta(
                request.getEstadoCuenta() != null ? request.getEstadoCuenta() : "ACTIVA");
        usuario.setRol(rol);

        return usuarioJpaRepository.save(usuario);
    }

    @Transactional
    public UsuarioJpa modificarMiCuenta(Long idUsuario, ActualizarMiCuentaRequest request) {
        UsuarioJpa usuario = usuarioJpaRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getNombreUsuario() != null && !request.getNombreUsuario().isBlank()) {
            usuario.setNombreUsuario(request.getNombreUsuario());
        }

        if (request.getContrasenha() != null && !request.getContrasenha().isBlank()) {
            usuario.setContrasenha(request.getContrasenha());
        }

        return usuarioJpaRepository.save(usuario);
    }

    @Transactional
    public UsuarioJpa modificarUsuarioAdmin(Long idUsuario, ActualizarUsuarioAdminRequest request) {
        validarAdmin(request.getIdRolSolicitante());

        UsuarioJpa usuario = usuarioJpaRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getNombreUsuario() != null && !request.getNombreUsuario().isBlank()) {
            usuario.setNombreUsuario(request.getNombreUsuario());
        }

        if (request.getContrasenha() != null && !request.getContrasenha().isBlank()) {
            usuario.setContrasenha(request.getContrasenha());
        }

        if (request.getEstadoCuenta() != null && !request.getEstadoCuenta().isBlank()) {
            usuario.setEstadoCuenta(request.getEstadoCuenta());
        }

        if (request.getIdRol() != null) {
            RolJpa rol = rolJpaRepository.findById(request.getIdRol())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            usuario.setRol(rol);
        }

        return usuarioJpaRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuarioAdmin(Long idUsuario, Long idRolSolicitante) {
        validarAdmin(idRolSolicitante);

        UsuarioJpa usuario = usuarioJpaRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioJpaRepository.delete(usuario);
    }

    private void validarAdmin(Long idRolSolicitante) {
        if (idRolSolicitante == null || !idRolSolicitante.equals(1L)) {
            throw new RuntimeException("No tiene permisos de administrador");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request.getNombreUsuario() == null || request.getNombreUsuario().isBlank()) {
            throw new RuntimeException("El nombre de usuario es obligatorio");
        }

        if (request.getContrasenha() == null || request.getContrasenha().isBlank()) {
            throw new RuntimeException("La contrasenha es obligatoria");
        }

        UsuarioJpa usuario = usuarioJpaRepository.findByNombreUsuarioIgnoreCase(request.getNombreUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario o contrasenha incorrectos"));

        if (!usuario.getContrasenha().equals(request.getContrasenha())) {
            throw new RuntimeException("Usuario o contrasenha incorrectos");
        }

        if (!"ACTIVA".equalsIgnoreCase(usuario.getEstadoCuenta())) {
            throw new RuntimeException("La cuenta se encuentra inactiva");
        }

        return LoginResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .estadoCuenta(usuario.getEstadoCuenta())
                .idRol(usuario.getRol() != null ? usuario.getRol().getIdRol() : null)
                .nombreRol(usuario.getRol() != null ? usuario.getRol().getNombreRol() : null)
                .mensaje("Login correcto")
                .build();
    }
}
