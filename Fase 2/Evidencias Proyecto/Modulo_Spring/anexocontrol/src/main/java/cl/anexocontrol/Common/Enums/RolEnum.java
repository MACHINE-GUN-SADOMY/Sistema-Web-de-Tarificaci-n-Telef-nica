package cl.anexocontrol.Common.Enums;

import lombok.Getter;

@Getter
public enum RolEnum {
    ADMINISTRADOR(1),
    EMPLEADO(2);

    private final Integer id;

    RolEnum(Integer id) {
        this.id = id;
    }
}
