package cl.anexocontrol.Rol.repository;

import cl.anexocontrol.Rol.Repository.Jpa.RolJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolJpaRepository extends JpaRepository<RolJpa, Long> {
}
