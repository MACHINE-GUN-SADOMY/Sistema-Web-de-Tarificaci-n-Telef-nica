-- 1. asigna automaticamente la fecha proceso al insertar en registro_llamada
CREATE OR REPLACE TRIGGER trg_fecha_proceso_registro
    BEFORE INSERT ON registro_llamada
    FOR EACH ROW
BEGIN
    IF :NEW.fecha_proceso IS NULL THEN
        :NEW.fecha_proceso := SYSDATE;
    END IF;
END;

-- 2. asigna automaticamente la fecha proceso al insertar en reporte_tarificacion
CREATE OR REPLACE TRIGGER trg_fecha_procesado_reporte
    BEFORE INSERT ON reporte_tarificacion
    FOR EACH ROW
BEGIN
    IF :NEW.fecha_procesado IS NULL THEN
        :NEW.fecha_procesado := SYSDATE;
    END IF;
END;

-- 3. deja en pendiente automaticamente al momento de crearse una solicitud de reporte
CREATE OR REPLACE TRIGGER trg_estado_solicitud_default
    BEFORE INSERT ON solicitud_reporte
    FOR EACH ROW
BEGIN
    IF :NEW.estado_solicitud IS NULL THEN
        :NEW.estado_solicitud := 'PENDIENTE';
    END IF;
END;

-- 4. evita colocar una duracion invalida
CREATE OR REPLACE TRIGGER trg_validar_duracion
    BEFORE INSERT OR UPDATE ON registro_llamada
    FOR EACH ROW
BEGIN
    IF :NEW.duracion_segundos IS NULL OR :NEW.duracion_segundos <= 0 THEN
        RAISE_APPLICATION_ERROR(-20500, 'La duración de la llamada debe ser mayor a 0.');
    END IF;
END;

-- 5. trigger de automatizacion de de tarificacion
-- al momento de entrar el registro por Python, se lanza el trigger automatico
CREATE OR REPLACE TRIGGER trg_tarificar_registro
    AFTER INSERT ON registro_llamada
    FOR EACH ROW
BEGIN
    INSERT INTO reporte_tarificacion (
        id_reporte_tarificacion,
        id_carga,
        anexo,
        duracion_total_segundos,
        proveedor,
        costo_calculado,
        fecha_procesado,
        id_usuario,
        id_registro_reporte,
        id_tipo_llamada
    ) VALUES (
                 seq_reporte_tarificacion.NEXTVAL,
                 :NEW.id_carga,
                 :NEW.anexo,
                 :NEW.duracion_segundos,
                 :NEW.proveedor,
                 fn_calcular_costo(
                         :NEW.id_tipo_llamada,
                         :NEW.proveedor,
                         :NEW.duracion_segundos
                 ),
                 SYSDATE,
                 :NEW.id_usuario,
                 :NEW.id_registro_llamada,
                 :NEW.id_tipo_llamada
             );
    COMMIT;
END;
