package cl.anexocontrol.Rol.Repository.Jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// jpa puro para mapear la tabla rol
@Entity
@Table(name = "rol")
@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
public class RolJpa {

    @Id
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre")
    private String nombreRol;
}
