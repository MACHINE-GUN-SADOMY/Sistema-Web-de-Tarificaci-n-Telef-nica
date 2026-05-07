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

-- 2. secuencia de usuario
CREATE SEQUENCE seq_usuario
    START WITH 1 -- por ahora comienza con 4
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 3. secuencia de carga
CREATE SEQUENCE seq_carga
    START WITH 1003
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;