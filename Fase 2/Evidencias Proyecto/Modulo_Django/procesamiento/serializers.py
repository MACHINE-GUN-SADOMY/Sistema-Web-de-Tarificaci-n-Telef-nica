from procesamiento.models import ReporteTarificacion
from rest_framework import serializers
from rest_framework.serializers import ModelSerializer


# dto pa pedir que se muestre un reporte
class MostrarReporteTarificacionRequest(serializers.ModelSerializer):
    idReporteTarificacion = serializers.IntegerField()

# dto pa pedir que se borre un reporte
class EliminarReporteTarificacionRequest(serializers.ModelSerializer):
    idReporteTarificacion = serializers.IntegerField()

# usamos este serialisers.Serializer para definir campos
# este serializer (dto) valida lo que nos mandara spring
class ProcesarArchivoRequest(serializers.Serializer):
    idSolicitud = serializers.IntegerField(required=True)
    idCarga = serializers.IntegerField(required=True)
    idUsuario = serializers.IntegerField(required=True)
    idTipoReporte = serializers.IntegerField(required=True)
    rutaArchivo = serializers.CharField(required=True, max_length=500)

    def validate_rutaArchivo(self, rutaArchivo):
        if not rutaArchivo.strip():
            raise serializers.ValidationError("La ruta del archivo es obligatoria")
        return rutaArchivo

# mas easy lo use practicamente para que tome las propiedades del ModelORM
class MostrarReporteTarificacionResponse(ModelSerializer):
    class Meta:
        model = ReporteTarificacion
        fields = "__all__"

# dto pa devolver la respuesta despues de procesar
class ProcesarArchivoResponse(serializers.Serializer):
    mensaje = serializers.CharField()
    idSolicitud = serializers.IntegerField()
    idCarga = serializers.IntegerField()



