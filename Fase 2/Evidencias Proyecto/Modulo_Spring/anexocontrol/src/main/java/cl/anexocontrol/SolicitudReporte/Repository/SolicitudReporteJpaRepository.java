package cl.anexocontrol.SolicitudReporte.Repository;

import cl.anexocontrol.SolicitudReporte.Repository.Jpa.SolicitudReporteJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudReporteJpaRepository extends JpaRepository<SolicitudReporteJpa, Long> {
}
