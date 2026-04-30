pr_tarificar_registro
CREATE OR REPLACE PROCEDURE pr_tarificar_registro (
    p_id_registro_llamada IN NUMBER
) IS
    -- variables del registro_llamada
    v_id_carga              NUMBER;
    v_anexo                 VARCHAR2(100);
    v_duracion_segundos     NUMBER;
    v_proveedor             VARCHAR2(100);
    v_fecha_proceso         DATE;
    v_id_usuario            NUMBER;
    v_id_tipo_llamada       NUMBER;

    -- variables de entorno
    v_existe                NUMBER;
    v_costo_calculado       NUMBER(10,2);
    v_total_consumo_carga   NUMBER(10,2);
    v_cantidad_llamadas     NUMBER(10);
    v_duracion_total        NUMBER(10);
    v_promedio_duracion     NUMBER(10,2);
BEGIN
    -- 1) verificar que el registro exista
SELECT
    id_registro_llamada
INTO
    v_existe
FROM registro_llamada
WHERE id_registro_llamada = p_id_registro_llamada;

-- ERROR POR SI LO NO ENCUENTRA
IF v_existe = 0 THEN
        RAISE_APPLICATION_ERROR(-20100, 'No existe el registro_llamada indicado.');
END IF;

    -- 2) verificar que no esté ya tarificado
SELECT
    p_id_registro_llamada
INTO
    v_existe
FROM reporte_tarificacion
WHERE id_registro_reporte = p_id_registro_llamada;

IF v_existe > 0 THEN
        RAISE_APPLICATION_ERROR(-20101, 'El registro ya fue tarificado.');
END IF;

    -- 3) obtener datos del registro
SELECT id_carga,
       anexo,
       duracion_segundos,
       proveedor,
       fecha_proceso,
       id_usuario,
       id_tipo_llamada
INTO v_id_carga,
    v_anexo,
    v_duracion_segundos,
    v_proveedor,
    v_fecha_proceso,
    v_id_usuario,
    v_id_tipo_llamada
FROM registro_llamada
WHERE id_registro_llamada = p_id_registro_llamada;

-- 4) calcular costo
v_costo_calculado := fn_calcular_costo(
            v_id_tipo_llamada,
            v_proveedor,
            v_duracion_segundos
                         );

    -- métricas resumen
    v_costo_calculado   := fn_calcular_costo(v_id_tipo_llamada, v_proveedor, v_duracion_segundos);
    v_cantidad_llamadas := fn_cantidad_llamadas_usuario(v_id_usuario, v_id_carga);
    v_duracion_total    := fn_duracion_total_usuario(v_id_usuario, v_id_carga);
    v_promedio_duracion := fn_promedio_duracion_usuario(v_id_usuario, v_id_carga);

    -- insertar en reporte_tarificacion
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
    id_tipo_llamada,
    cant_llamadas_usuario,
    duracion_total_segundos,
    prom_duracion_llamada
) VALUES (
             seq_reporte_tarificacion.NEXTVAL,
             v_id_carga,
             v_anexo,
             v_duracion_segundos,
             v_proveedor,
             v_costo_calculado,
             v_fecha_proceso,
             v_id_usuario,
             p_id_registro_llamada,
             v_id_tipo_llamada,
             v_cantidad_llamadas,
             v_duracion_total,
             v_promedio_duracion
         );

EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20104, 'No se encontraron datos del registro a tarificar.');
WHEN DUP_VAL_ON_INDEX THEN
            RAISE_APPLICATION_ERROR(-20105, 'Ya existe una tarificación para ese registro.');
WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20106, 'Error en pr_tarificar_registro: ' || SQLERRM);
END;