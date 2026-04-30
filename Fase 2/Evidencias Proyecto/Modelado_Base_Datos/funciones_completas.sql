-- CALCULAR COSTO
CREATE OR REPLACE FUNCTION fn_calcular_costo (
    p_tipo_llamada IN NUMBER,p_proveedor IN VARCHAR2,p_duracion_segundos IN NUMBER
) RETURN NUMBER
IS
    v_costo_total        NUMBER(38,2);
    v_costo_tipo_llamada NUMBER(38,2);
    v_factor_proveedor   NUMBER(38,2);
BEGIN
    -- si la duracion son 0 segundos o es nulo, devolvemos 0
    IF p_duracion_segundos IS NULL OR p_duracion_segundos <= 0 THEN
        RETURN 0;
    END IF;

        -- FACTOR DEL TIPO DE LLAMADA
    CASE p_tipo_llamada
            WHEN 1 THEN
                v_costo_tipo_llamada := 10; -- LOCAL
    WHEN 2 THEN
                v_costo_tipo_llamada := 15; -- NACIONAL
    WHEN 3 THEN
                v_costo_tipo_llamada := 40; -- INTERNACIONAL
    WHEN 4 THEN
                v_costo_tipo_llamada := 20; -- MOVIL
    ELSE
                v_costo_tipo_llamada := 0;
    END CASE;

        -- FACTOR DEL PROVEEDOR
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

        -- COSTO TOTAL ES IGUAL A : DURACION DE LLAMADA * TIPO DE LLAMADA * PROVEEDOR
    v_costo_total := p_duracion_segundos * v_costo_tipo_llamada * v_factor_proveedor;

    RETURN ROUND(v_costo_total, 2);

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20010, 'Error en fn_calcular_costo: ' || SQLERRM);
END;

-- fn_cantidad_llamadas_usuario
CREATE OR REPLACE FUNCTION fn_cantidad_llamadas_usuario (
    p_id_usuario IN NUMBER,p_id_carga   IN NUMBER
) RETURN NUMBER
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
        id_usuario = p_id_usuario AND id_carga = p_id_carga;

    RETURN v_cantidad_llamadas;

    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20011, 'Error en fn_cantidad_llamadas_usuario: ' || SQLERRM);
END;

-- fn_cantidad_llamadas_usuario_total
CREATE OR REPLACE FUNCTION fn_cantidad_llamadas_usuario_total (
    p_id_usuario IN NUMBER
) RETURN NUMBER
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
END;

-- fn_duracion_total_usuario
CREATE OR REPLACE FUNCTION fn_duracion_total_usuario (
    p_id_usuario IN NUMBER,p_id_carga   IN NUMBER
) RETURN NUMBER
    IS
    v_total_segundos NUMBER(10);
BEGIN
    SELECT
        NVL(SUM(duracion_segundos),0)
    INTO
        v_total_segundos
    FROM
        registro_llamada
    WHERE
        id_usuario = p_id_usuario AND id_carga = p_id_carga;

    RETURN v_total_segundos;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20013, 'Error en fn_duracion_total_usuario: ' || SQLERRM);
END;

-- fn promedio duracion usuario
CREATE OR REPLACE FUNCTION fn_promedio_duracion_usuario (
    p_id_usuario IN NUMBER,p_id_carga   IN NUMBER
) RETURN NUMBER
    IS
    v_promedio_llamada NUMBER(10,2);
BEGIN
    SELECT
        NVL(AVG(duracion_segundos),0)
    INTO
        v_promedio_llamada
    FROM
        registro_llamada
    WHERE
        id_usuario = p_id_usuario AND id_carga = p_id_carga;

    RETURN ROUND(v_promedio_llamada, 2);

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20014, 'Error en fn_promedio_duracion_usuario: ' || SQLERRM);
END;
