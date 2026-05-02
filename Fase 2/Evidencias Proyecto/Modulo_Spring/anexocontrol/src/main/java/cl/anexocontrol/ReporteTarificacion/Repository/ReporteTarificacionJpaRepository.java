package cl.anexocontrol.ReporteTarificacion.Repository;

import cl.anexocontrol.ReporteTarificacion.Repository.Jpa.ReporteTarificacionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReporteTarificacionJpaRepository extends JpaRepository<ReporteTarificacionJpa, Long> {
}
