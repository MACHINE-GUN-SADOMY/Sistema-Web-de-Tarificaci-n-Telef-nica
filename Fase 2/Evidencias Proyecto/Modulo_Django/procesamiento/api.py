from procesamiento.serializers import ProcesarArchivoRequest
from procesamiento.service import procesarArchivoListo
from rest_framework import serializers
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView

class ProcesamientoCompletado(APIView):
    def post(self,request):
        serializer = ProcesarArchivoRequest(data=request.data)

        if not serializer.is_valid():
            return Response(
                {"mensaje" : "Datos invalidos",
                "errores" : serializer.errors})

        try:
            resultado = procesarArchivoListo(serializer.validated_data)

            return Response(resultado, status=status.HTTP_202_ACCEPTED) # no se crea un nuevo recurso, solo se valida

        # si falla la validacion dara este error con el serializer
        except serializers.ValidationError as exception:
            return Response({
                "mensaje" : "Error de validacion",
                "detalle" : str(exception)
            },
            status=status.HTTP_400_BAD_REQUEST)