from rest_framework import serializers

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


class ProcesarArchivoResponse(serializers.Serializer):
    mensaje = serializers.CharField()
    idSolicitud = serializers.IntegerField()
    idCarga = serializers.IntegerField()