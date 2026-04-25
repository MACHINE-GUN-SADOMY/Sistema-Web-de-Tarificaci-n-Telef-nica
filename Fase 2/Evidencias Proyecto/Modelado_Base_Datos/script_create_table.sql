-- DROP TABLES
DROP TABLE reporte_tarificacion CASCADE CONSTRAINTS;
DROP TABLE registro_llamada CASCADE CONSTRAINTS;
DROP TABLE solicitud_reporte CASCADE CONSTRAINTS;
DROP TABLE usuario CASCADE CONSTRAINTS;
DROP TABLE tipo_llamada CASCADE CONSTRAINTS;
DROP TABLE tipo_reporte CASCADE CONSTRAINTS;
DROP TABLE rol CASCADE CONSTRAINTS;

-- CREACION DE TABLAS Y PK
CREATE TABLE tipo_reporte (
    id_tipo_reporte NUMBER(10) NOT NULL,
    nombre          VARCHAR2(100) NOT NULL
);
ALTER TABLE tipo_reporte ADD CONSTRAINT pk_tipo_reporte PRIMARY KEY (id_tipo_reporte);

CREATE TABLE rol (
    id_rol  NUMBER(10) NOT NULL,
    nombre  VARCHAR2(30) NOT NULL
);
ALTER TABLE rol ADD CONSTRAINT pk_rol PRIMARY KEY (id_rol);

CREATE TABLE tipo_llamada (
    id_tipo_llamada NUMBER(10) NOT NULL,
    nombre          VARCHAR2(30) NOT NULL
);
ALTER TABLE tipo_llamada ADD CONSTRAINT pk_tipo_llamada PRIMARY KEY (id_tipo_llamada);

CREATE TABLE usuario (
    id_usuario      NUMBER(10) NOT NULL,
    nombre_usuario  VARCHAR2(100) NOT NULL,
    contrasenha     VARCHAR2(100) NOT NULL,
    estado_cuenta   VARCHAR2(30) NOT NULL,
    id_rol          NUMBER(10) NOT NULL
);
ALTER TABLE usuario ADD CONSTRAINT pk_usuario PRIMARY KEY (id_usuario);

CREATE TABLE solicitud_reporte (
    id_solicitud      NUMBER(10) NOT NULL,
    id_carga          NUMBER(10) NOT NULL,
    fecha_solicitud   DATE NOT NULL,
    estado_solicitud  VARCHAR2(20) NOT NULL,
    ruta_reporte      VARCHAR2(255) NOT NULL,
    id_usuario        NUMBER(10) NOT NULL,
    id_tipo_reporte   NUMBER(10) NOT NULL
);
ALTER TABLE solicitud_reporte ADD CONSTRAINT pk_solicitud_reporte PRIMARY KEY (id_solicitud);

CREATE TABLE registro_llamada (
    id_registro_llamada NUMBER(10) NOT NULL,
    id_carga            NUMBER(10) NOT NULL,
    anexo               VARCHAR2(100) NOT NULL,
    numero_destino      NUMBER(10) NOT NULL,
    duracion_segundos   NUMBER(10) NOT NULL,
    proveedor           VARCHAR2(100) NOT NULL,
    fecha_llamada       DATE NOT NULL,
    fecha_proceso       DATE NOT NULL,
    id_usuario          NUMBER(10) NOT NULL,
    id_tipo_llamada     NUMBER(10) NOT NULL
);
ALTER TABLE registro_llamada ADD CONSTRAINT pk_registro_llamada PRIMARY KEY (id_registro_llamada);

CREATE TABLE reporte_tarificacion (
    id_reporte_tarificacion         NUMBER(10) NOT NULL,
    id_carga                        NUMBER(10) NOT NULL,
    anexo                           VARCHAR2(100) NOT NULL,
    duracion_total_segundos         NUMBER(10) NOT NULL,
    proveedor                       VARCHAR2(100) NOT NULL,
    cant_llamadas_usuario           NUMBER(10) NOT NULL,
    cant_total_llamadas_usuario     NUMBER(10) NOT NULL,
    total_tiempo_carga              NUMBER(10) NOT NULL,
    prom_duracion_llamada           NUMBER(10) NOT NULL,
    fecha_procesado                 DATE NOT NULL,
    costo_calculado                 NUMBER(10,2) NOT NULL,
    id_usuario                      NUMBER(10) NOT NULL,
    id_registro_reporte             NUMBER(10) NOT NULL,
    id_tipo_llamada                 NUMBER(10) NOT NULL
);
ALTER TABLE reporte_tarificacion ADD CONSTRAINT pk_reporte_tarificacion PRIMARY KEY (id_reporte_tarificacion);

-- CREACION DE FK
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol);

ALTER TABLE solicitud_reporte ADD CONSTRAINT fk_solrep_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);

ALTER TABLE solicitud_reporte ADD CONSTRAINT fk_solrep_tipo_reporte FOREIGN KEY (id_tipo_reporte) REFERENCES tipo_reporte (id_tipo_reporte);

ALTER TABLE registro_llamada ADD CONSTRAINT fk_regll_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);

ALTER TABLE registro_llamada ADD CONSTRAINT fk_regll_tipo_llamada FOREIGN KEY (id_tipo_llamada) REFERENCES tipo_llamada (id_tipo_llamada);

ALTER TABLE reporte_tarificacion ADD CONSTRAINT fk_reptar_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);

ALTER TABLE reporte_tarificacion ADD CONSTRAINT fk_reptar_registro FOREIGN KEY (id_registro_reporte) REFERENCES registro_llamada (id_registro_llamada);

ALTER TABLE reporte_tarificacion ADD CONSTRAINT fk_reptar_tipo_llamada FOREIGN KEY (id_tipo_llamada) REFERENCES tipo_llamada (id_tipo_llamada);

-- ONE TO ONE
ALTER TABLE reporte_tarificacion ADD CONSTRAINT uq_reptar_registro UNIQUE (id_registro_reporte);