-- delete from
DELETE FROM reporte_tarificacion;
DELETE FROM registro_llamada;
DELETE FROM solicitud_reporte;
DELETE FROM usuario;
DELETE FROM tipo_llamada;
DELETE FROM tipo_reporte;
DELETE FROM rol;

-- TABLAS MAESTRAS
INSERT INTO rol (id_rol, nombre) VALUES (1, 'ADMINISTRADOR');
INSERT INTO rol (id_rol, nombre) VALUES (2, 'EMPLEADO');

INSERT INTO tipo_reporte (id_tipo_reporte, nombre) VALUES (1, 'PDF');
INSERT INTO tipo_reporte (id_tipo_reporte, nombre) VALUES (2, 'CSV');

INSERT INTO tipo_llamada (id_tipo_llamada, nombre) VALUES (1, 'LOCAL');
INSERT INTO tipo_llamada (id_tipo_llamada, nombre) VALUES (2, 'NACIONAL');
INSERT INTO tipo_llamada (id_tipo_llamada, nombre) VALUES (3, 'INTERNACIONAL');
INSERT INTO tipo_llamada (id_tipo_llamada, nombre) VALUES (4, 'MOVIL');

-- USUARIOS
INSERT INTO usuario (
    id_usuario,
    nombre_usuario,
    contrasenha,
    estado_cuenta,
    id_rol
) VALUES (
    1, 'admin', 'admin123', 'ACTIVA', 1
);

INSERT INTO usuario (
    id_usuario,
    nombre_usuario,
    contrasenha,
    estado_cuenta,
    id_rol
) VALUES (
    2, 'kristian', 'kristian123', 'ACTIVA', 2
);

INSERT INTO usuario (
    id_usuario,
    nombre_usuario,
    contrasenha,
    estado_cuenta,
    id_rol
) VALUES (
    3, 'empleado1', 'empleado123', 'ACTIVA', 2
);

-- SOLICITUDES DE REPORTE
INSERT INTO solicitud_reporte (
    id_solicitud,
    id_carga,
    fecha_solicitud,
    estado_solicitud,
    ruta_reporte,
    id_usuario,
    id_tipo_reporte
) VALUES (
    1, 1001, DATE '2026-04-24', 'PENDIENTE', '/archivos/reportes/reporte_usuario_2.pdf', 2, 1
);

INSERT INTO solicitud_reporte (
    id_solicitud,
    id_carga,
    fecha_solicitud,
    estado_solicitud,
    ruta_reporte,
    id_usuario,
    id_tipo_reporte
) VALUES (
    2, 1002, DATE '2026-04-24', 'LISTO', '/archivos/reportes/reporte_usuario_3.csv', 3, 2
);

-- REGISTROS DE LLAMADA
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
    5, 1001, '101', 987111111, 45, 'ENTEL', DATE '2026-04-22', DATE '2026-04-24', 2, 1
);

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
    6, 1001, '101', 987222222, 90, 'MOVISTAR', DATE '2026-04-22', DATE '2026-04-24', 2, 2
);

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
    7, 1001, '101', 987333333, 180, 'CLARO', DATE '2026-04-22', DATE '2026-04-24', 2, 3
);

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
    8, 1001, '101', 987444444, 30, 'WOM', DATE '2026-04-22', DATE '2026-04-24', 2, 4
);

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
    9, 1001, '102', 988111111, 75, 'ENTEL', DATE '2026-04-22', DATE '2026-04-24', 2, 1
);

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
    10, 1001, '102', 988222222, 210, 'MOVISTAR', DATE '2026-04-22', DATE '2026-04-24', 2, 2
);

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
    11, 1001, '102', 988333333, 360, 'CLARO', DATE '2026-04-22', DATE '2026-04-24', 2, 3
);

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
    12, 1001, '102', 988444444, 50, 'WOM', DATE '2026-04-22', DATE '2026-04-24', 2, 4
);

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
    13, 1002, '201', 989111111, 20, 'ENTEL', DATE '2026-04-23', DATE '2026-04-24', 3, 1
);

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
    14, 1002, '201', 989222222, 65, 'MOVISTAR', DATE '2026-04-23', DATE '2026-04-24', 3, 2
);

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
    15, 1002, '201', 989333333, 140, 'CLARO', DATE '2026-04-23', DATE '2026-04-24', 3, 3
);

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
    16, 1002, '201', 989444444, 240, 'WOM', DATE '2026-04-23', DATE '2026-04-24', 3, 4
);

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
    17, 1002, '202', 990111111, 15, 'ENTEL', DATE '2026-04-23', DATE '2026-04-24', 3, 1
);

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
    18, 1002, '202', 990222222, 95, 'MOVISTAR', DATE '2026-04-23', DATE '2026-04-24', 3, 2
);

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
    19, 1002, '202', 990333333, 275, 'CLARO', DATE '2026-04-23', DATE '2026-04-24', 3, 3
);

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
    20, 1002, '202', 990444444, 600, 'WOM', DATE '2026-04-23', DATE '2026-04-24', 3, 4
);

COMMIT;

-- PROBAR TABLAS
SELECT * FROM rol;
SELECT * FROM tipo_llamada;
SELECT * FROM tipo_reporte;
SELECT * FROM reporte_tarificacion;
SELECT * FROM registro_llamada;
SELECT * FROM solicitud_reporte;
SELECT * FROM usuario;