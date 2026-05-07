package cl.anexocontrol.SolicitudReporte.Repository;

import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudReporteJpaRepository extends JpaRepository<SolicitudReporteJpa, Long> {
    List<SolicitudReporteJpa> findByIdUsuario(Long idUsuario);

    List<SolicitudReporteJpa> findByIdCarga(Long idCarga);
}
