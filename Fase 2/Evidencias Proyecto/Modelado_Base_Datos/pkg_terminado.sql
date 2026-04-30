-- 1. CREAR SECUENCIAS
CREATE SEQUENCE seq_solicitud_reporte
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_error_proceso
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_reporte_tarificacion
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
-- FUNCIONA

-- 2. CREAR PACKAGE
CREATE OR REPLACE PACKAGE pkg_tarificacion
IS
    FUNCTION fn_calcular_costo
    (p_tipo_llamada IN NUMBER, p_proveedor IN VARCHAR2, p_duracion_segundos IN NUMBER)
        RETURN NUMBER;

    FUNCTION fn_cantidad_llamadas_usuario
    (p_id_usuario IN NUMBER, p_id_carga IN NUMBER)
        RETURN NUMBER;

    FUNCTION fn_cantidad_llamadas_usuario_total
    (p_id_usuario IN NUMBER)
        RETURN NUMBER;

    FUNCTION fn_duracion_total_usuario
    (p_id_usuario IN NUMBER, p_id_carga IN NUMBER)
        RETURN NUMBER;

    FUNCTION fn_promedio_duracion_usuario
    (p_id_usuario IN NUMBER, p_id_carga IN NUMBER)
        RETURN NUMBER;

    PROCEDURE pr_insertar_registro_limpio
    (p_id_registro_llamada IN NUMBER,
     p_id_carga            IN NUMBER,
     p_anexo               IN VARCHAR2,
     p_numero_destino      IN NUMBER,
     p_duracion_segundos   IN NUMBER,
     p_proveedor           IN VARCHAR2,
     p_fecha_llamada       IN DATE,
     p_fecha_proceso       IN DATE,
     p_id_usuario          IN NUMBER,
     p_id_tipo_llamada     IN NUMBER);

    PROCEDURE pr_generar_nueva_solicitud
    (p_id_carga        IN NUMBER,
     p_id_usuario      IN NUMBER,
     p_id_tipo_reporte IN NUMBER,
     p_ruta_reporte    IN VARCHAR2);

    PROCEDURE pr_actualizar_estado_solicitud
    (p_id_solicitud     IN NUMBER,
     p_estado_solicitud IN VARCHAR2,
     p_ruta_reporte     IN VARCHAR2 DEFAULT NULL);

    PROCEDURE pr_registrar_error
    (p_modulo_origen        IN VARCHAR2,
     p_procedimiento_origen IN VARCHAR2,
     p_mensaje_error        IN VARCHAR2,
     p_id_usuario           IN NUMBER   DEFAULT NULL,
     p_id_carga             IN NUMBER   DEFAULT NULL,
     p_id_registro_llamada  IN NUMBER   DEFAULT NULL,
     p_detalle_error        IN VARCHAR2 DEFAULT NULL);

    PROCEDURE pr_tarificar_carga
    (p_id_carga IN NUMBER);
END pkg_tarificacion;
/ -- FUNCIONA

-- 3. CREAR BODY
CREATE OR REPLACE PACKAGE BODY pkg_tarificacion
IS
    FUNCTION fn_calcular_costo
    (p_tipo_llamada IN NUMBER, p_proveedor IN VARCHAR2, p_duracion_segundos IN NUMBER)
        RETURN NUMBER
        IS
        v_costo_total        NUMBER(38,2);
        v_costo_tipo_llamada NUMBER(38,2);
        v_factor_proveedor   NUMBER(38,2);
    BEGIN
        IF p_duracion_segundos IS NULL OR p_duracion_segundos <= 0 THEN
            RETURN 0;
        END IF;

        CASE p_tipo_llamada
            WHEN 1 THEN
                v_costo_tipo_llamada := 10;
            WHEN 2 THEN
                v_costo_tipo_llamada := 15;
            WHEN 3 THEN
                v_costo_tipo_llamada := 40;
            WHEN 4 THEN
                v_costo_tipo_llamada := 20;
            ELSE
                v_costo_tipo_llamada := 0;
            END CASE;

        CASE UPPER(p_proveedor)
            WHEN 'ENTEL' THEN
                v_factor_proveedor := 1.00;
            WHEN 'MOVISTAR' THEN
                v_factor_proveedor := 1.05;
            WHEN 'CLARO' THEN
                v_factor_proveedor := 1.08;
            WHEN 'WOM' THEN
                v_factor_proveedor := 1.03;
            ELSE
                v_factor_proveedor := 1.00;
            END CASE;

        v_costo_total := p_duracion_segundos * v_costo_tipo_llamada * v_factor_proveedor;

        RETURN ROUND(v_costo_total, 2);

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20010, 'Error en fn_calcular_costo: ' || SQLERRM);
    END fn_calcular_costo;

    FUNCTION fn_cantidad_llamadas_usuario
    (p_id_usuario IN NUMBER, p_id_carga IN NUMBER)
        RETURN NUMBER
        IS
        v_cantidad_llamadas NUMBER(10);
    BEGIN
        SELECT
            COUNT(id_registro_llamada)
        INTO
            v_cantidad_llamadas
        FROM
            registro_llamada
        WHERE
            id_usuario = p_id_usuario
          AND id_carga = p_id_carga;

        RETURN v_cantidad_llamadas;

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20011, 'Error en fn_cantidad_llamadas_usuario: ' || SQLERRM);
    END fn_cantidad_llamadas_usuario;

    FUNCTION fn_cantidad_llamadas_usuario_total
    (p_id_usuario IN NUMBER)
        RETURN NUMBER
        IS
        v_cantidad_llamadas NUMBER(10);
    BEGIN
        SELECT
            COUNT(id_registro_llamada)
        INTO
            v_cantidad_llamadas
        FROM
            registro_llamada
        WHERE
            id_usuario = p_id_usuario;

        RETURN v_cantidad_llamadas;

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20012, 'Error en fn_cantidad_llamadas_usuario_total: ' || SQLERRM);
    END fn_cantidad_llamadas_usuario_total;

    FUNCTION fn_duracion_total_usuario
    (p_id_usuario IN NUMBER, p_id_carga IN NUMBER)
        RETURN NUMBER
        IS
        v_total_segundos NUMBER(10);
    BEGIN
        SELECT
            NVL(SUM(duracion_segundos), 0)
        INTO
            v_total_segundos
        FROM
            registro_llamada
        WHERE
            id_usuario = p_id_usuario
          AND id_carga = p_id_carga;

        RETURN v_total_segundos;

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20013, 'Error en fn_duracion_total_usuario: ' || SQLERRM);
    END fn_duracion_total_usuario;

    FUNCTION fn_promedio_duracion_usuario
    (p_id_usuario IN NUMBER, p_id_carga IN NUMBER)
        RETURN NUMBER
        IS
        v_promedio_llamada NUMBER(10,2);
    BEGIN
        SELECT
            NVL(AVG(duracion_segundos), 0)
        INTO
            v_promedio_llamada
        FROM
            registro_llamada
        WHERE
            id_usuario = p_id_usuario
          AND id_carga = p_id_carga;

        RETURN ROUND(v_promedio_llamada, 2);

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20014, 'Error en fn_promedio_duracion_usuario: ' || SQLERRM);
    END fn_promedio_duracion_usuario;

    PROCEDURE pr_insertar_registro_limpio
    (p_id_registro_llamada IN NUMBER,
     p_id_carga            IN NUMBER,
     p_anexo               IN VARCHAR2,
     p_numero_destino      IN NUMBER,
     p_duracion_segundos   IN NUMBER,
     p_proveedor           IN VARCHAR2,
     p_fecha_llamada       IN DATE,
     p_fecha_proceso       IN DATE,
     p_id_usuario          IN NUMBER,
     p_id_tipo_llamada     IN NUMBER)
        IS
        v_existe NUMBER;
    BEGIN
        IF p_id_registro_llamada IS NULL THEN
            RAISE_APPLICATION_ERROR(-20001, 'El id_registro_llamada no puede ser nulo.');
        END IF;

        IF p_id_carga IS NULL THEN
            RAISE_APPLICATION_ERROR(-20002, 'El id_carga no puede ser nulo.');
        END IF;

        IF p_duracion_segundos IS NULL OR p_duracion_segundos <= 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'La duracion_segundos debe ser mayor a 0.');
        END IF;

        IF p_proveedor IS NULL THEN
            RAISE_APPLICATION_ERROR(-20004, 'El proveedor no puede ser nulo.');
        END IF;

        SELECT
            COUNT(*)
        INTO
            v_existe
        FROM
            usuario
        WHERE
            id_usuario = p_id_usuario;

        IF v_existe = 0 THEN
            RAISE_APPLICATION_ERROR(-20005, 'El usuario no existe.');
        END IF;

        INSERT INTO registro_llamada (
            id_registro_llamada,
            id_carga,
            anexo,
            numero_destino,
            duracion_segundos,
            proveedor,
            fecha_llamada,
            fecha_proceso,
            id_usuario,
            id_tipo_llamada
        ) VALUES (
                     p_id_registro_llamada,
                     p_id_carga,
                     p_anexo,
                     p_numero_destino,
                     p_duracion_segundos,
                     UPPER(p_proveedor),
                     p_fecha_llamada,
                     p_fecha_proceso,
                     p_id_usuario,
                     p_id_tipo_llamada
                 );

        COMMIT;

    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            pkg_tarificacion.pr_registrar_error(
                    'MODULO IMPORTACION',
                    'pr_insertar_registro_limpio',
                    'Ya existe un registro con ese id_registro_llamada',
                    p_id_usuario,
                    p_id_carga,
                    p_id_registro_llamada,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20100, 'Ya existe un registro con ese id_registro_llamada.');

        WHEN OTHERS THEN
            pkg_tarificacion.pr_registrar_error(
                    'MODULO IMPORTACION',
                    'pr_insertar_registro_limpio',
                    'Error al insertar registro limpio',
                    p_id_usuario,
                    p_id_carga,
                    p_id_registro_llamada,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20101, 'Error en pr_insertar_registro_limpio: ' || SQLERRM);
    END pr_insertar_registro_limpio;

    PROCEDURE pr_generar_nueva_solicitud
    (p_id_carga        IN NUMBER,
     p_id_usuario      IN NUMBER,
     p_id_tipo_reporte IN NUMBER,
     p_ruta_reporte    IN VARCHAR2)
        IS
        v_existe_usuario      NUMBER;
        v_existe_tipo_reporte NUMBER;
    BEGIN
        SELECT
            COUNT(*)
        INTO
            v_existe_usuario
        FROM
            usuario
        WHERE
            id_usuario = p_id_usuario;

        IF v_existe_usuario = 0 THEN
            RAISE_APPLICATION_ERROR(-20200, 'El usuario no existe.');
        END IF;

        SELECT
            COUNT(*)
        INTO
            v_existe_tipo_reporte
        FROM
            tipo_reporte
        WHERE
            id_tipo_reporte = p_id_tipo_reporte;

        IF v_existe_tipo_reporte = 0 THEN
            RAISE_APPLICATION_ERROR(-20201, 'El tipo de reporte no existe.');
        END IF;

        INSERT INTO solicitud_reporte (
            id_solicitud,
            id_carga,
            fecha_solicitud,
            estado_solicitud,
            ruta_reporte,
            id_usuario,
            id_tipo_reporte
        ) VALUES (
                     seq_solicitud_reporte.NEXTVAL,
                     p_id_carga,
                     SYSDATE,
                     'PENDIENTE',
                     p_ruta_reporte,
                     p_id_usuario,
                     p_id_tipo_reporte
                 );

    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            pkg_tarificacion.pr_registrar_error(
                    'MODULO REPORTES',
                    'pr_generar_solicitud_reporte',
                    'Ya existe una solicitud duplicada',
                    p_id_usuario,
                    p_id_carga,
                    NULL,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20202, 'Ya existe una solicitud con ese identificador.');

        WHEN OTHERS THEN
            pkg_tarificacion.pr_registrar_error(
                    'MODULO REPORTES',
                    'pr_generar_solicitud_reporte',
                    'Error al generar solicitud de reporte',
                    p_id_usuario,
                    p_id_carga,
                    NULL,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20203, 'Error en pr_generar_solicitud_reporte: ' || SQLERRM);
    END pr_generar_nueva_solicitud;

    PROCEDURE pr_actualizar_estado_solicitud
    (p_id_solicitud     IN NUMBER,
     p_estado_solicitud IN VARCHAR2,
     p_ruta_reporte     IN VARCHAR2 DEFAULT NULL)
        IS
        v_existe_solicitud NUMBER;
    BEGIN
        SELECT
            COUNT(*)
        INTO
            v_existe_solicitud
        FROM
            solicitud_reporte
        WHERE
            id_solicitud = p_id_solicitud;

        IF v_existe_solicitud = 0 THEN
            RAISE_APPLICATION_ERROR(-20300, 'La solicitud de reporte no existe.');
        END IF;

        IF UPPER(p_estado_solicitud) NOT IN ('PENDIENTE', 'GENERANDO', 'LISTO', 'ERROR') THEN
            RAISE_APPLICATION_ERROR(-20301, 'Estado de solicitud no valido.');
        END IF;

        UPDATE solicitud_reporte
        SET estado_solicitud = UPPER(p_estado_solicitud),
            ruta_reporte = CASE
                               WHEN p_ruta_reporte IS NOT NULL THEN p_ruta_reporte
                               ELSE ruta_reporte
                END
        WHERE id_solicitud = p_id_solicitud;

    EXCEPTION
        WHEN OTHERS THEN
            pkg_tarificacion.pr_registrar_error(
                    'MODULO REPORTES',
                    'pr_actualizar_estado_solicitud',
                    'Error al actualizar el estado de la solicitud',
                    NULL,
                    NULL,
                    NULL,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20302, 'Error en pr_actualizar_estado_solicitud: ' || SQLERRM);
    END pr_actualizar_estado_solicitud;

    PROCEDURE pr_registrar_error
    (p_modulo_origen        IN VARCHAR2,
     p_procedimiento_origen IN VARCHAR2,
     p_mensaje_error        IN VARCHAR2,
     p_id_usuario           IN NUMBER   DEFAULT NULL,
     p_id_carga             IN NUMBER   DEFAULT NULL,
     p_id_registro_llamada  IN NUMBER   DEFAULT NULL,
     p_detalle_error        IN VARCHAR2 DEFAULT NULL)
        IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        INSERT INTO error_proceso (
            id_error,
            modulo_origen,
            procedimiento_origen,
            mensaje_error,
            fecha_error,
            id_usuario,
            id_carga,
            id_registro_llamada,
            detalle_error
        ) VALUES (
                     seq_error_proceso.NEXTVAL,
                     p_modulo_origen,
                     p_procedimiento_origen,
                     p_mensaje_error,
                     SYSDATE,
                     p_id_usuario,
                     p_id_carga,
                     p_id_registro_llamada,
                     p_detalle_error
                 );

        COMMIT;

    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR(-20400, 'Error en pr_registrar_error: ' || SQLERRM);
    END pr_registrar_error;

    PROCEDURE pr_tarificar_carga
    (p_id_carga IN NUMBER)
        IS
        CURSOR cur_registros IS
            SELECT
                rl.id_registro_llamada,
                rl.id_carga,
                rl.anexo,
                rl.numero_destino,
                rl.duracion_segundos,
                rl.proveedor,
                rl.fecha_proceso,
                rl.id_usuario,
                rl.id_tipo_llamada
            FROM
                registro_llamada rl
            WHERE
                rl.id_carga = p_id_carga;

        v_existe_carga          NUMBER(10);
        v_registros_tarificados NUMBER(10);
        v_costo_calculado       NUMBER(10,2);
        v_cant_llamadas_usuario NUMBER(10);
        v_cant_total_llamadas   NUMBER(10);
        v_total_tiempo_carga    NUMBER(10);
        v_prom_duracion         NUMBER(10,2);
    BEGIN
        SELECT
            COUNT(id_registro_llamada)
        INTO
            v_existe_carga
        FROM
            registro_llamada
        WHERE
            id_carga = p_id_carga;

        IF v_existe_carga = 0 THEN
            RAISE_APPLICATION_ERROR(-20500, 'No existen registros para la carga indicada.');
        END IF;

        SELECT
            COUNT(id_reporte_tarificacion)
        INTO
            v_registros_tarificados
        FROM
            reporte_tarificacion
        WHERE
            id_carga = p_id_carga;

        IF v_registros_tarificados > 0 THEN
            RAISE_APPLICATION_ERROR(-20501, 'La carga indicada ya fue tarificada.');
        END IF;

        FOR reg IN cur_registros LOOP
                v_costo_calculado := pkg_tarificacion.fn_calcular_costo(
                        reg.id_tipo_llamada,
                        reg.proveedor,
                        reg.duracion_segundos
                                     );

                v_cant_llamadas_usuario := pkg_tarificacion.fn_cantidad_llamadas_usuario(
                        reg.id_usuario,
                        reg.id_carga
                                           );

                v_cant_total_llamadas := pkg_tarificacion.fn_cantidad_llamadas_usuario_total(
                        reg.id_usuario
                                         );

                SELECT
                    NVL(SUM(duracion_segundos), 0)
                INTO
                    v_total_tiempo_carga
                FROM
                    registro_llamada
                WHERE
                    id_carga = reg.id_carga;

                v_prom_duracion := pkg_tarificacion.fn_promedio_duracion_usuario(
                        reg.id_usuario,
                        reg.id_carga
                                   );

                INSERT INTO reporte_tarificacion (
                    id_reporte_tarificacion,
                    id_carga,
                    anexo,
                    duracion_total_segundos,
                    proveedor,
                    cant_llamadas_usuario,
                    cant_total_llamadas_usuario,
                    total_tiempo_carga,
                    prom_duracion_llamada,
                    costo_calculado,
                    fecha_procesado,
                    id_usuario,
                    id_registro_reporte,
                    id_tipo_llamada
                ) VALUES (
                             seq_reporte_tarificacion.NEXTVAL,
                             reg.id_carga,
                             reg.anexo,
                             reg.duracion_segundos,
                             reg.proveedor,
                             v_cant_llamadas_usuario,
                             v_cant_total_llamadas,
                             v_total_tiempo_carga,
                             v_prom_duracion,
                             v_costo_calculado,
                             SYSDATE,
                             reg.id_usuario,
                             reg.id_registro_llamada,
                             reg.id_tipo_llamada
                         );
            END LOOP;

    EXCEPTION
        WHEN OTHERS THEN
            pkg_tarificacion.pr_registrar_error(
                    'MODULO TARIFICACION',
                    'pr_tarificar_carga',
                    'Error al tarificar la carga',
                    NULL,
                    p_id_carga,
                    NULL,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20502, 'Error en pr_tarificar_carga: ' || SQLERRM);
    END pr_tarificar_carga;
END pkg_tarificacion;
/
-- funciona

-- 4. CREAR TRIGGERS
CREATE OR REPLACE TRIGGER trg_fecha_proceso_registro
    BEFORE INSERT ON registro_llamada
    FOR EACH ROW
BEGIN
    IF :NEW.fecha_proceso IS NULL THEN
        :NEW.fecha_proceso := SYSDATE;
    END IF;
END;

CREATE OR REPLACE TRIGGER trg_fecha_procesado_reporte
    BEFORE INSERT ON reporte_tarificacion
    FOR EACH ROW
BEGIN
    IF :NEW.fecha_procesado IS NULL THEN
        :NEW.fecha_procesado := SYSDATE;
    END IF;
END;

CREATE OR REPLACE TRIGGER trg_estado_solicitud_default
    BEFORE INSERT ON solicitud_reporte
    FOR EACH ROW
BEGIN
    IF :NEW.estado_solicitud IS NULL THEN
        :NEW.estado_solicitud := 'PENDIENTE';
    END IF;
END;

CREATE OR REPLACE TRIGGER trg_validar_duracion
    BEFORE INSERT OR UPDATE ON registro_llamada
    FOR EACH ROW
BEGIN
    IF :NEW.duracion_segundos IS NULL OR :NEW.duracion_segundos <= 0 THEN
        RAISE_APPLICATION_ERROR(-20500, 'La duracion de la llamada debe ser mayor a 0.');
    END IF;
END;
/
-- funciona

-- 5. EJECUTAR PROCEDIMIENTOS
BEGIN
    pkg_tarificacion.pr_insertar_registro_limpio(
            21,
            1001,
            '301',
            991112223,
            120,
            'ENTEL',
            DATE '2026-04-24',
            DATE '2026-04-24',
            2,
            1
    );
END;

BEGIN
    pkg_tarificacion.pr_generar_nueva_solicitud(
            1500,
            2,
            1,
            '/archivos/reportes/reporte_usuario_2.pdf'
    );
END;

BEGIN
    pkg_tarificacion.pr_actualizar_estado_solicitud(
            1,
            'GENERANDO'
    );
END;

BEGIN
    pkg_tarificacion.pr_actualizar_estado_solicitud(
            1,
            'LISTO',
            '/archivos/reportes/reporte_usuario_2.pdf'
    );
END;

BEGIN
    pkg_tarificacion.pr_registrar_error(
            'MODULO TARIFICACION',
            'pr_tarificar_registro',
            'Error de prueba al tarificar registro',
            2,
            1001,
            5,
            'Detalle de prueba para validar insercion en error_proceso'
    );
END;

BEGIN
    pkg_tarificacion.pr_tarificar_carga(1001);
END;

-- 6. REVISAR RESULTADOS
SELECT * FROM registro_llamada;
SELECT * FROM solicitud_reporte;
SELECT * FROM reporte_tarificacion;
SELECT * FROM error_proceso;

-- 7. ELIMINAR PKG Y TRIGGERS Y SEQUENCE
-- DROP PACKAGE pkg_tarificacion;
-- DROP TRIGGER trg_fecha_proceso_registro;
-- DROP TRIGGER trg_fecha_procesado_reporte;
-- DROP TRIGGER trg_estado_solicitud_default;
-- DROP TRIGGER trg_validar_duracion;
-- DROP SEQUENCE seq_reporte_tarificacion;
-- DROP SEQUENCE seq_solicitud_reporte;
-- DROP SEQUENCE seq_error_proceso;

-- DELETE FROM para eliminar el contenido
-- FROM error_proceso;
-- DELETE FROM registro_llamada;
-- DELETE FROM reporte_tarificacion;
-- DELETE FROM solicitud_reporte;
-- DELETE FROM usuario;
-- DELETE FROM rol;
-- DELETE FROM tipo_llamada;
-- DELETE FROM tipo_reporte;