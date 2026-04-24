# CREACION DE TABLAS Y PK
CREATE TABLE tipo_reporte (
    id_tipo_reporte     NUMERIC(10) NOT NULL,
    nombre              VARCHAR(100) NOT NULL);
ALTER TABLE tipo_reporte ADD CONSTRAINT PK_TIPO_REPORTE PRIMARY KEY (id_tipo_reporte);

CREATE TABLE rol (
    id_rol              NUMERIC(10) NOT NULL,
    nombre              VARCHAR(30) NOT NULL);
ALTER TABLE rol ADD CONSTRAINT PK_rol PRIMARY KEY (id_rol);

CREATE TABLE tipo_llamada (
    id_tipo_llamada     NUMERIC(10) NOT NULL,
    nombre              VARCHAR(30) NOT NULL);
ALTER TABLE tipo_llamada ADD CONSTRAINT PK_tipo_llamada PRIMARY KEY (id_tipo_llamada);

CREATE TABLE usuario (
    id_usuario          NUMERIC(10) NOT NULL,
    nombre_usuario      VARCHAR(100) NOT NULL,
    contrasenha         VARCHAR(100) NOT NULL,
    estado_cuenta       VARCHAR(30) NOT NULL,
    id_rol              NUMERIC(10) NOT NULL);
ALTER TABLE usuario ADD CONSTRAINT PK_usuario PRIMARY KEY (id_usuario);

CREATE TABLE solicitud_reporte (
    id_solicitud        NUMERIC(10) NOT NULL,
    fecha_solicitud     DATE NOT NULL,
    estado_solicitud    VARCHAR(20) NOT NULL,
    ruta_reporte        VARCHAR(100) NOT NULL,
    id_usuario          NUMERIC(10) NOT NULL,
    id_tipo_reporte     NUMERIC(10) NOT NULL);
ALTER TABLE solicitud_reporte ADD CONSTRAINT PK_solicitud_reporte PRIMARY KEY (id_solicitud);

CREATE TABLE registro_llamada (
    id_registro_llamada NUMERIC(10) NOT NULL,
    anexo               VARCHAR(100) NOT NULL,
    numero_destino      NUMERIC(10) NOT NULL,
    duracion_segundos   INTEGER(255) NOT NULL,
    proveedor           VARCHAR(100) NOT NULL,
    fecha_llamada       DATE NOT NULL,
    fecha_proceso       DATE NOT NULL,
    id_usuario          NUMERIC(10) NOT NULL,
    id_tipo_llamada     NUMERIC(10) NOT NULL);
ALTER TABLE registro_llamada ADD CONSTRAINT PK_registro_llamada PRIMARY KEY (id_registro_llamada);

CREATE TABLE reporte_tarificacion (
    id_reporte_tarificacion NUMERIC(10) NOT NULL,
    anexo                   VARCHAR(100) NOT NULL,
    duracion_total_segundos INTEGER(255) NOT NULL,
    proveedor               VARCHAR(100) NOT NULL,
    costo_calculado         INTEGER(255) NOT NULL,
    fecha_procesado         DATE NOT NULL,
    id_usuario              NUMERIC(10) NOT NULL,
    id_registro_reporte     NUMERIC(10) NOT NULL,
    id_tipo_llamada         NUMERIC(10) NOT NULL);
ALTER TABLE reporte_tarificacion ADD CONSTRAINT PK_reporte_tarificacion PRIMARY KEY (id_reporte_tarificacion);

# CREACION DE FK
ALTER TABLE solicitud_reporte ADD CONSTRAINT FK_id_tipo_reporte_solicitud FOREIGN KEY (id_tipo_reporte) REFERENCES tipo_reporte (id_tipo_reporte);
ALTER TABLE usuario ADD CONSTRAINT FK_id_rol_usuario FOREIGN KEY (id_rol) REFERENCES rol (id_rol);
ALTER TABLE solicitud_reporte ADD CONSTRAINT FK_id_usuario_solicitud FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);
ALTER TABLE registro_llamada ADD CONSTRAINT FK_id_usuario_registro FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);
ALTER TABLE registro_llamada ADD CONSTRAINT FK_id_tipo_llamada_registro FOREIGN KEY (id_tipo_llamada) REFERENCES tipo_llamada (id_tipo_llamada);
ALTER TABLE reporte_tarificacion ADD CONSTRAINT FK_id_tipo_llamada_reporte FOREIGN KEY (id_tipo_llamada) REFERENCES tipo_llamada (id_tipo_llamada);
ALTER TABLE reporte_tarificacion ADD CONSTRAINT FK_id_registro_llamada_reporte FOREIGN KEY (id_registro_reporte) REFERENCES registro_llamada (id_registro_llamada);
ALTER TABLE reporte_tarificacion ADD CONSTRAINT FK_id_usuario_reporte FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);


# ONE TO ONE
ALTER TABLE reporte_tarificacion ADD CONSTRAINT uq_rep_tar UNIQUE (id_registro_reporte);

# HACER DROPPER
DROP TABLE reporte_tarificacion;
DROP TABLE registro_llamada;
DROP TABLE solicitud_reporte;
DROP TABLE usuario;
DROP TABLE tipo_llamada;
DROP TABLE tipo_reporte;
DROP TABLE rol;

