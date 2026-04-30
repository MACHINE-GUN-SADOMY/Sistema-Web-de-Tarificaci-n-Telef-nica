-- 1. sequencia de solicitud reporte
CREATE SEQUENCE seq_solicitud_reporte
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 2. secuencia de error
CREATE SEQUENCE seq_error_proceso
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;