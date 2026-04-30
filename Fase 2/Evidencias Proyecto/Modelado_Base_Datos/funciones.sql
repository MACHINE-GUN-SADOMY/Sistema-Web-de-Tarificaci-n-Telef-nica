SET SERVEROUTPUT ON;

-- Calcular Costo
DECLARE
    -- parametros para probar
    p_tipo_llamada              NUMERIC(10) :=1;
    p_proveedor                 VARCHAR2(10) :='CLARO';
    p_duracion_segundos         NUMERIC(38) := 60;

    -- variables de entorno
    v_costo_total               NUMERIC(38,2);
    v_costo_tipo_llamada        NUMERIC(38,2);
    v_factor_proveedor          NUMERIC(38,2);
BEGIN
    -- si la duracion son 0 segundos o es nulo, devolvemos 0
    IF p_duracion_segundos IS NULL OR p_duracion_segundos <=0 THEN
        -- RETURN 0;
        DBMS_OUTPUT.PUT_LINE('X');
    END IF;

    -- FACTOR DEL TIPO DE LLAMADA
    CASE p_tipo_llamada
        WHEN 1 THEN
            v_costo_tipo_llamada :=10; -- LOCAL
        WHEN 2 THEN
            v_costo_tipo_llamada :=15; -- NACIONAL
        WHEN 3 THEN
            v_costo_tipo_llamada :=40; -- INTERNACIONAL
        WHEN 4 THEN
            v_costo_tipo_llamada :=20; -- MOVIL
        ELSE
            v_costo_tipo_llamada :=0;
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

    -- AQUI IRIA EL EXCEPTION

    DBMS_OUTPUT.PUT_LINE('VALOR TOTAL: ' || ROUND(v_costo_total, 2));

    -- RETURN ROUND(v_costo_total, 2);
END;

-- FUNCION cantidad llamadas usuario
SET SERVEROUTPUT ON;

DECLARE
    p_id_usuario        NUMERIC(10) :=2;
    p_id_carga          NUMERIC(10) :=1001;

    -- variable de entorno
    v_cantidad_llamadas NUMERIC(10);
BEGIN
    SELECT
        COUNT(id_registro_llamada)
    INTO
        v_cantidad_llamadas
    FROM
        registro_llamada
    WHERE
        id_usuario = p_id_usuario AND id_carga = p_id_carga;

    -- AQUI IRIA EL EXCEPTION

    DBMS_OUTPUT.PUT_LINE('CANTIDAD DE LLAMADAS DEL USUARIO ' || p_id_usuario ||
        ' EN LA CARGA ' || p_id_carga || ': ' || v_cantidad_llamadas);

    -- AQUI IRIA EL RETURN
END;

-- funcion para contar llamadas totales de un usuario
SET SERVEROUTPUT ON;

DECLARE
    p_id_usuario        NUMERIC(10) :=2;

    -- variable de entorno
    v_cantidad_llamadas NUMERIC(10);
BEGIN
    SELECT
        COUNT(id_registro_llamada)
    INTO
        v_cantidad_llamadas
    FROM
        registro_llamada
    WHERE
        id_usuario = p_id_usuario;

    -- AQUI IRIA EL EXCEPTION

    DBMS_OUTPUT.PUT_LINE('total de segundos del usuario ' || p_id_usuario
                             || ' total de llamdas: ' || v_cantidad_llamadas);

    -- AQUI IRIA EL RETURN
END;

-- funcion para sumar todos los segundos de una llamada en una carga
SET SERVEROUTPUT ON;

DECLARE
    p_id_usuario        NUMERIC(10) :=2;
    p_id_carga          NUMERIC(10) :=1001;

    -- variable de entorno
    v_total_segundos NUMERIC(10);
BEGIN
    SELECT
        NVL(SUM(duracion_segundos),0)
    INTO
        v_total_segundos
    FROM
        registro_llamada
    WHERE
        id_usuario = p_id_usuario AND id_carga = p_id_carga;

    -- AQUI IRIA EL EXCEPTION

    DBMS_OUTPUT.PUT_LINE('Total de segundos de llamada ' || v_total_segundos
        || ' usuario: ' || p_id_usuario);

    -- AQUI IRIA EL RETURN
END;

-- fn_promedio_duracion_usuario
SET SERVEROUTPUT ON;

DECLARE
    p_id_usuario        NUMERIC(10) :=2;
    p_id_carga          NUMERIC(10) :=1001;

    -- variable de entorno
    v_promedio_llamada NUMERIC(10);
BEGIN
    SELECT
        NVL(AVG(duracion_segundos),0)
    INTO
        v_promedio_llamada
    FROM
        registro_llamada
    WHERE
        id_usuario = p_id_usuario AND id_carga = p_id_carga;

    -- AQUI IRIA EL EXCEPTION

    DBMS_OUTPUT.PUT_LINE('Promedio de la duracion de las llamadas ' || ROUND(v_promedio_llamada)
        || ' usuario: ' || p_id_usuario);

    -- AQUI IRIA EL RETURN
END;

