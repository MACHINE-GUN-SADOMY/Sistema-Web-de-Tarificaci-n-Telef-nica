package cl.anexocontrol.RegistroLlamada.repository;

import cl.anexocontrol.RegistroLlamada.Repository.Jpa.RegistroLlamadaJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroLlamadaJpaRepository extends JpaRepository<RegistroLlamadaJpa, Long> {
}
