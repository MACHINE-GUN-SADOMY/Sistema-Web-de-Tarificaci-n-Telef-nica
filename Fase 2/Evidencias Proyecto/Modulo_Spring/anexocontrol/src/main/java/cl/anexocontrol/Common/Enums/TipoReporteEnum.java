package cl.anexocontrol.Common.enums;

import lombok.Getter;

@Getter
public enum TipoReporteEnum {
    PDF(1),
    CSV(2),

    private final Integer id;

    TipoReporteEnum(Integer id) {
        this.id = id;
    }
}
