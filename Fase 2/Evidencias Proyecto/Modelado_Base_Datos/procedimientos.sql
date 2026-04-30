-- pr_insertar_registro_limpio
-- este procedimiento sirve para insertar un registro limpio, luego de la solicitud.
CREATE OR REPLACE PROCEDURE pr_insertar_registro_limpio(
    p_id_registro_llamada IN NUMBER,
    p_id_carga            IN NUMBER,
    p_anexo               IN VARCHAR2,
    p_numero_destino      IN NUMBER,
    p_duracion_segundos   IN NUMBER,
    p_proveedor           IN VARCHAR2,
    p_fecha_llamada       IN DATE,
    p_fecha_proceso       IN DATE,
    p_id_usuario          IN NUMBER,
    p_id_tipo_llamada     IN NUMBER
) IS v_existe NUMBER;
BEGIN
    -- validaciones basicas
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

    -- Validar que exista el usuario
    SELECT
        COUNT(*)
    INTO
        v_existe
    FROM usuario
        WHERE id_usuario = p_id_usuario;

    IF v_existe = 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'El usuario no existe.');
    END IF;

    -- Insertar registro limpio
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

    --EXCEPTION
        --WHEN DUP_VAL_ON_INDEX THEN
            --RAISE_APPLICATION_ERROR(-20007, 'Ya existe un registro con ese id_registro_llamada.');
        --WHEN OTHERS THEN
            --RAISE_APPLICATION_ERROR(-20099, 'Error en pr_insertar_registro_limpio: ' || SQLERRM);

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        pr_registrar_error(
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
        pr_registrar_error(
                'MODULO IMPORTACION',
                'pr_insertar_registro_limpio',
                'Error al insertar registro limpio',
                p_id_usuario,
                p_id_carga,
                p_id_registro_llamada,
                SQLERRM
        );
        RAISE_APPLICATION_ERROR(-20101, 'Error en pr_insertar_registro_limpio: ' || SQLERRM);
END;

-- EJECUTAR PR INSERTAR
BEGIN
    pr_insertar_registro_limpio(
            21,
            1003,
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

-- pr_generar_solicitud_reporte
-- este procedimiento sirve para crear una nueva solicitud de reporte
CREATE OR REPLACE PROCEDURE pr_generar_nueva_solicitud(
    p_id_carga        IN NUMBER,
    p_id_usuario      IN NUMBER,
    p_id_tipo_reporte IN NUMBER,
    p_ruta_reporte    IN VARCHAR2
) IS
    v_existe_usuario      NUMBER;
    v_existe_tipo_reporte NUMBER;
BEGIN
    -- validar que si realmente existe el user
    SELECT
        COUNT(*)
    INTO
        v_existe_usuario
    FROM usuario
        WHERE id_usuario = p_id_usuario;

    -- sino existe el usuario
    IF v_existe_usuario = 0 THEN
        RAISE_APPLICATION_ERROR(-20200, 'El usuario no existe.');
    END IF;

    -- validar sino existe el tipo de reporte
    SELECT
        COUNT(*)
    INTO
        v_existe_tipo_reporte
    FROM tipo_reporte
        WHERE id_tipo_reporte = p_id_tipo_reporte;

    -- sino existe el tipo de reporte lance error
    IF v_existe_tipo_reporte = 0 THEN
        RAISE_APPLICATION_ERROR(-20201, 'El tipo de reporte no existe.');
    END IF;

    -- insertar solicitud de reporte
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

    --EXCEPTION
        --WHEN DUP_VAL_ON_INDEX THEN
            --RAISE_APPLICATION_ERROR(-20202, 'Ya existe una solicitud con ese identificador.');
        --WHEN OTHERS THEN
            --RAISE_APPLICATION_ERROR(-20203, 'Error en pr_generar_solicitud_reporte: ' || SQLERRM);

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        pr_registrar_error(
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
        pr_registrar_error(
                'MODULO REPORTES',
                'pr_generar_solicitud_reporte',
                'Error al generar solicitud de reporte',
                p_id_usuario,
                p_id_carga,
                NULL,
                SQLERRM
        );
        RAISE_APPLICATION_ERROR(-20203, 'Error en pr_generar_solicitud_reporte: ' || SQLERRM);
END;

-- probar pr
BEGIN
    pr_generar_nueva_solicitud(
            1500,
            2,
            1,
            '/archivos/reportes/reporte_usuario_2.pdf'
    );
END;

-- pr_actualizar_estado_solicitud
-- este procedimiento sirve para cambiar el estado de la solicitud
CREATE OR REPLACE PROCEDURE pr_actualizar_estado_solicitud (
    p_id_solicitud     IN NUMBER,
    p_estado_solicitud IN VARCHAR2,
    p_ruta_reporte     IN VARCHAR2 DEFAULT NULL
) IS
    v_existe_solicitud NUMBER;
BEGIN
    -- validar que exista la solicitud
    SELECT
        COUNT(*)
    INTO
        v_existe_solicitud
    FROM solicitud_reporte
        WHERE id_solicitud = p_id_solicitud;

    -- sino existe la soli , manda error
    IF v_existe_solicitud = 0 THEN
        RAISE_APPLICATION_ERROR(-20300, 'La solicitud de reporte no existe.');
    END IF;

    -- validar estado permitido
    IF UPPER(p_estado_solicitud) NOT IN ('PENDIENTE', 'GENERANDO', 'LISTO', 'ERROR') THEN
        RAISE_APPLICATION_ERROR(-20301, 'Estado de solicitud no valido.');
    END IF;
    -- con esto restringimos estados que no esten en la condicional

    -- actualizar estado y ruta si corresponde
    UPDATE solicitud_reporte
    SET estado_solicitud = UPPER(p_estado_solicitud),
        ruta_reporte = CASE
                           WHEN p_ruta_reporte IS NOT NULL THEN p_ruta_reporte
                           ELSE ruta_reporte
            END
    WHERE id_solicitud = p_id_solicitud;

    --EXCEPTION
        --WHEN OTHERS THEN
            --RAISE_APPLICATION_ERROR(-20302, 'Error en pr_actualizar_estado_solicitud: ' || SQLERRM);

    EXCEPTION
        WHEN OTHERS THEN
            pr_registrar_error(
                    'MODULO REPORTES',
                    'pr_actualizar_estado_solicitud',
                    'Error al actualizar el estado de la solicitud',
                    NULL,
                    NULL,
                    NULL,
                    SQLERRM
            );
            RAISE_APPLICATION_ERROR(-20302, 'Error en pr_actualizar_estado_solicitud: ' || SQLERRM);
END;

-- probar pr
-- cambiar solo estado
BEGIN
    pr_actualizar_estado_solicitud(
            1,
            'GENERANDO'
    );
END;

-- cambiar estado y ruta
BEGIN
    pr_actualizar_estado_solicitud(
            1,
            'LISTO',
            '/archivos/reportes/reporte_usuario_2.pdf'
    );
END;

-- pr_registrar_error
-- este procedimiento sirve para ser llamado e inyectar datos con los problems de exceptions
CREATE OR REPLACE PROCEDURE pr_registrar_error (
    p_modulo_origen        IN VARCHAR2,
    p_procedimiento_origen IN VARCHAR2,
    p_mensaje_error        IN VARCHAR2,
    p_id_usuario           IN NUMBER   DEFAULT NULL,
    p_id_carga             IN NUMBER   DEFAULT NULL,
    p_id_registro_llamada  IN NUMBER   DEFAULT NULL,
    p_detalle_error        IN VARCHAR2 DEFAULT NULL
) IS
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

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20400, 'Error en pr_registrar_error: ' || SQLERRM);
END;

-- probar procedimiento
BEGIN
    pr_registrar_error(
            'MODULO TARIFICACION',
            'pr_tarificar_registro',
            'Error de prueba al tarificar registro',
            2,
            1001,
            5,
            'Detalle de prueba para validar inserción en error_proceso'
    );
END;

-- pr_tarificacion_registro
-- este procedimiento sirve para tarificar el registro, python lo llamara una vez pandas termine de
-- insertar las columnas y filas de registro llamadas
CREATE OR REPLACE PROCEDURE pr_tarificar_carga (
    p_id_carga IN NUMBER
) IS
    -- cursor de registros pertenecientes a la carga
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

    -- variables de control
    v_existe_carga          NUMBER(10);
    v_registros_tarificados NUMBER(10);

    -- variables de cálculo
    v_costo_calculado       NUMBER(10,2);
    v_cant_llamadas_usuario NUMBER(10);
    v_cant_total_llamadas   NUMBER(10);
    v_total_tiempo_carga    NUMBER(10);
    v_prom_duracion         NUMBER(10,2);

BEGIN
    -- 1) verificar que existan registros para la carga
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

    -- 2) verificar que la carga no haya sido tarificada antes
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

    -- 3) recorrer todos los registros de la carga
    FOR reg IN cur_registros LOOP

            -- costo individual de la llamada
            v_costo_calculado := fn_calcular_costo(
                    reg.id_tipo_llamada,
                    reg.proveedor,
                    reg.duracion_segundos
                                 );

            -- cantidad de llamadas del usuario dentro de la carga
            v_cant_llamadas_usuario := fn_cantidad_llamadas_usuario(
            reg.id_usuario,
            reg.id_carga);

            -- cantidad total histórica de llamadas del usuario
            v_cant_total_llamadas := fn_cantidad_llamadas_usuario_total(
            reg.id_usuario);

            -- total de tiempo de la carga completa
            SELECT
                NVL(SUM(duracion_segundos), 0)
            INTO
                v_total_tiempo_carga
            FROM
                registro_llamada
            WHERE
                id_carga = reg.id_carga;

            -- promedio de duración del usuario dentro de la carga
            v_prom_duracion := fn_promedio_duracion_usuario(
            reg.id_usuario, reg.id_carga);

            -- insertar registro tarificado
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
                         reg.id_tipo_llamada);
        END LOOP;

    EXCEPTION
        WHEN OTHERS THEN
            pr_registrar_error(
                    'MODULO TARIFICACION',
                    'pr_tarificar_carga',
                    'Error al tarificar la carga',
                    NULL,
                    p_id_carga,
                    NULL,
                    SQLERRM);
            RAISE_APPLICATION_ERROR(-20502, 'Error en pr_tarificar_carga: ' || SQLERRM);
END;

-- probar procedimiento
BEGIN
    pr_tarificar_carga(1001);
END;
