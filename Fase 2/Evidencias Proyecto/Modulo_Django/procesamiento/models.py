from django.db import models

# aqui tendremos las entidades (al estilo spring)

# Entidad Rol
class Rol(models.Model):
    id_rol = models.IntegerField(primary_key=True, db_column='ID_ROL')
    nombre = models.CharField(max_length=30, db_column='NOMBRE')

    def __str__(self):
        return f"{self.nombre}"

    class Meta:
        managed = False
        db_table = "ROL"

# Entidad Usuario
class Usuario(models.Model):
    id_usuario = models.IntegerField(primary_key=True, db_column='ID_USUARIO')
    nombre_usuario = models.CharField(max_length=100, db_column='NOMBRE_USUARIO')
    contrasenha = models.CharField(max_length=30, db_column='CONTRASENHA')
    estado_cuenta = models.CharField(max_length=30, db_column='ESTADO_CUENTA')
    id_rol = models.ForeignKey(Rol, on_delete=models.DO_NOTHING, db_column='ID_ROL')

    def __str__(self):
        return f"{self.nombre_usuario}"

    class Meta:
        managed = False
        db_table = "USUARIO"

# Entidad tipo de llamada
class TipoLlamada(models.Model):
    id_tipo_llamada = models.IntegerField(primary_key=True, db_column='ID_TIPO_LLAMADA')
    nombre = models.CharField(max_length=30, db_column='NOMBRE')

    def __str__(self):
        return f"{self.nombre}"

    class Meta:
        managed = False
        db_table = "TIPO_LLAMADA"

# Entidad tipo reporte
class TipoReporte(models.Model):
    id_tipo_reporte = models.IntegerField(primary_key=True, db_column='ID_TIPO_REPORTE')
    nombre = models.CharField(max_length=30, db_column='NOMBRE')

    def __str__(self):
        return f"{self.nombre}"

    class Meta:
        managed = False
        db_table = "TIPO_REPORTE"

# Entidad Registro Llamada
class RegistroLlamada(models.Model):
    id_registro_llamada = models.IntegerField(primary_key=True, db_column='ID_REGISTRO_LLAMADA')
    id_carga = models.IntegerField(db_column='ID_CARGA')
    anexo = models.CharField(max_length=100, db_column='ANEXO')
    numero_destino = models.IntegerField(db_column='NUMERO_DESTINO')
    duracion_segundos = models.IntegerField(db_column='DURACION_SEGUNDOS')
    proveedor = models.CharField(max_length=100, db_column='PROVEEDOR')
    fecha_llamada = models.DateField(db_column='FECHA_LLAMADA')
    fecha_proceso = models.DateField(db_column='FECHA_PROCESO')
    # FK
    id_usuario = models.ForeignKey(Usuario, on_delete=models.DO_NOTHING, db_column='ID_USUARIO')
    id_tipo_llamada = models.ForeignKey(TipoLlamada, on_delete=models.DO_NOTHING, db_column='ID_TIPO_LLAMADA')

    def __str__(self):
        return f"{self.id_registro_llamada} Anexo: {self.anexo}"

    class Meta:
        managed = False
        db_table = "REGISTRO_LLAMADA"

class ReporteTarificacion(models.Model):
    id_reporte_tarificacion = models.IntegerField(primary_key=True, db_column='ID_REPORTE_TARIFICACION')
    id_carga = models.IntegerField(db_column='ID_CARGA')
    anexo = models.CharField(max_length=100, db_column='ANEXO')
    duracion_total_segundos = models.IntegerField(db_column='DURACION_TOTAL_SEGUNDOS')
    proveedor = models.CharField(max_length=100, db_column='PROVEEDOR')
    cant_llamadas_usuario = models.IntegerField(db_column='CANT_LLAMADAS_USUARIO')
    cant_total_llamadas_usuario = models.IntegerField(db_column='CANT_TOTAL_LLAMADAS_USUARIO')
    total_tiempo_carga = models.IntegerField(db_column='TOTAL_TIEMPO_CARGA')
    prom_duracion_llamada = models.IntegerField(db_column='PROM_DURACION_LLAMADA')
    costo_calculado = models.IntegerField(db_column='COSTO_CALCULADO')
    fecha_proceso = models.DateField(db_column='FECHA_PROCESADO')
    # FK
    id_usuario = models.ForeignKey(Usuario, on_delete=models.DO_NOTHING, db_column='ID_USUARIO')
    id_tipo_llamada = models.ForeignKey(TipoLlamada, on_delete=models.DO_NOTHING, db_column='ID_TIPO_LLAMADA')

    def __str__(self):
        return f"{self.id_reporte_tarificacion} Carga: {self.id_carga}"

    class Meta:
        managed = False
        db_table = "REPORTE_TARIFICACION"

class SolicitudReporte(models.Model):
    id_solicitud = models.IntegerField(primary_key=True, db_column='ID_SOLICITUD')
    id_carga = models.IntegerField(db_column='ID_CARGA')
    fecha_solicitud = models.DateField(db_column='FECHA_SOLICITUD')
    estado_solicitud = models.CharField(max_length=20, db_column='ESTADO_SOLICITUD')
    ruta_reporte = models.CharField(max_length=100, db_column='RUTA_REPORTE')
    # FK
    id_tipo_reporte = models.ForeignKey(TipoReporte, on_delete=models.DO_NOTHING, db_column='ID_TIPO_REPORTE')
    id_usuario = models.ForeignKey(Usuario, on_delete=models.DO_NOTHING, db_column='ID_USUARIO')

    def __str__(self):
        return f"{self.id_solicitud} Carga: {self.id_carga}"

    class Meta:
        managed = False
        db_table = "SOLICITUD_REPORTE"

class ErrorProceso(models.Model):
    id_error = models.IntegerField(primary_key=True, db_column='ID_ERROR')
    modulo_origen = models.CharField(max_length=100, db_column='MODULO_ORIGEN')
    procedimiento_origen = models.CharField(max_length=100, db_column='PROCEDIMIENTO_ORIGEN')
    mensaje_error = models.CharField(max_length=255, db_column='MENSAJE_ERROR')
    fecha_error = models.DateField(db_column='FECHA_ERROR')
    id_usuario = models.IntegerField(db_column='ID_USUARIO')
    id_carga = models.IntegerField(db_column='ID_CARGA')
    id_registro_llamada = models.IntegerField(db_column='ID_REGISTRO_LLAMADA')
    detalle_error = models.CharField(max_length=255, db_column='DETALLE_ERROR')

    def __str__(self):
        return f"{self.id_error} Carga: {self.id_carga} Mensaje Error: {self.mensaje_error}"

    class Meta:
        managed = False
        db_table = "ERROR_PROCESO"
