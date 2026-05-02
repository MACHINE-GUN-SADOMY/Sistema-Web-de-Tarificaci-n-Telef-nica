package cl.anexocontrol.Usuario.Repository;

import cl.anexocontrol.Usuario.Repository.Jpa.UsuarioJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, Long> {
}
