package cl.anexocontrol.ErrorProceso.Repository;

import cl.anexocontrol.ErrorProceso.Repository.Jpa.ErrorProcesoJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorProcesoJpaRepository extends JpaRepository<ErrorProcesoJpa, Long> {
}
