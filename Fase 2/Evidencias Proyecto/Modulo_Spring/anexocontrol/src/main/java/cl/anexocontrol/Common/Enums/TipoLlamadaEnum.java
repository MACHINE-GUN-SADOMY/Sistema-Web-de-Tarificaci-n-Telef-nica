package cl.anexocontrol.Common.enums;

import lombok.Getter;

@Getter
public enum TipoLlamadaEnum {
    LOCAL(1),
    NACIONAL(2),
    INTERNACIONAL(3),
    MOVIL(4);

    private final Integer id;

    TipoLlamadaEnum(Integer id) {
        this.id = id;
    }
}
