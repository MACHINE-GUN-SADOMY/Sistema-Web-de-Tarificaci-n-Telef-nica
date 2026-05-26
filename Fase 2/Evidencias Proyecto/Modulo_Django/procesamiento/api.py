from procesamiento.serializers import ProcesarArchivoRequest, MostrarReporteTarificacionResponse
from procesamiento.service import procesarArchivoTarificarCarga, mostrarTarificaciones, mostrarTarificacionPorId, \
    eliminarReportePorIdReporte
from rest_framework import serializers
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView

class MostrarTodasLasTarificaciones(APIView):
    def get(self, request):
        # aplicamos el catch de python , para capturar
        # los serializers validationErrror
        try:  # primero obtenemos los reportes
            reportes = mostrarTarificaciones()

            # construimos el builder response
            response_serializer = MostrarReporteTarificacionResponse(
                reportes, many=True)  # many true por que seran muchos objetos

            # retornamos el response , con el builder creado y el estatus 200 OK
            return Response(response_serializer.data, status=status.HTTP_200_OK)
        except serializers.ValidationError as exception:
            # devolvemos el response de django
            # con mensaje/detalle
            return Response({
                "mensaje": "Error de validacion",
                "detalle": str(exception)
            },
                # aqui el estado
                status=status.HTTP_400_BAD_REQUEST)

class MostrarTarifificacionPorIdReporte(APIView):
    def get(self, request, id):
        try:
            data = {"idReporteTarificacion" : id}

            reporte = mostrarTarificacionPorId(data)

            response_serializer = MostrarReporteTarificacionResponse(reporte, many=False)

            return Response(response_serializer.data, status=status.HTTP_200_OK)

        except serializers.ValidationError as exception:
            return Response({
                "mensaje" : "Error de validacion",
                "detalle" : str(exception),
            },status=status.HTTP_400_BAD_REQUEST)

class ProcesamientoLlamadas(APIView):
    # procesar , validar archivo
    def post(self,request):
        serializer = ProcesarArchivoRequest(data=request.data)

        if not serializer.is_valid():
            return Response(
                {"mensaje" : "Error de validacion",
                "errores" : serializer.errors})

        try:
            resultado = procesarArchivoTarificarCarga(serializer.validated_data)

            return Response(resultado, status=status.HTTP_202_ACCEPTED) # no se crea un nuevo recurso, solo se valida

        # si falla la validacion dara este error con el serializer
        except serializers.ValidationError as exception:
            return Response({
                "mensaje" : "Error de validacion",
                "detalle" : str(exception)
            },
            status=status.HTTP_400_BAD_REQUEST) # revisar codigo de error

class EliminarTarificacion(APIView):
    def delete(self,request,id):
        try:
            data = {"idReporteTarificacion" : id}

            reporteEliminado = eliminarReportePorIdReporte(data)

            if reporteEliminado is True:
                return Response({
                    "mensaje": "Reporte eliminado",
                },status=status.HTTP_204_NO_CONTENT)

        except serializers.ValidationError as exception:
            return Response({
            'mensaje' : 'Error Inesperado',
            'detalle' : str(exception),
            },
            status=status.HTTP_400_BAD_REQUEST) # revisar codigo de eror