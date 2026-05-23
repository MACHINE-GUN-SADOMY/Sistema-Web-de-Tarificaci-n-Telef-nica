import os
import csv

from rest_framework import serializers
from django.conf import settings
from rest_framework.exceptions import ValidationError

class GeneradorReportesService:
    rutaReporte = None
    idCarga = None
    idTipoReporte = None

    def generarReporte(self, reportes, idCarga: int, idTipoReporte: int):
        self.idCarga = idCarga
        self.idTipoReporte = idTipoReporte

        # si no hay reportes tarificacdos
        if reportes is None or not reportes.exists():
            raise ValidationError("No existen reportes.")

        # sino pasa lo primero segun el tipo de Tipo reporte se llamara el metodo

        # 1. PDF, 2. CSV
        if idTipoReporte == 2: # CSV
            return self.generarReporteCSV(reportes, idCarga)

        if idTipoReporte == 1: # PDF
            raise ValidationError("Generacion PDF aun no implementada")

        # si el tipo es incorrecto
        raise serializers.ValidationError("Tipo de reporte invalido.")

    # para generar los CSV
    def generarReporteCSV(self, reportes, idCarga: int):
        # se crea la carpeta en la base de la direccion
        carpeta_reportes = os.path.join(settings.BASE_DIR, "archivos","reportes")
        os.makedirs(carpeta_reportes, exist_ok=True)

        # se pone el nombre al archivo
        nombre_archivo = f"reporte_carga_{idCarga}.csv"
        ruta_reporte = os.path.join(carpeta_reportes, nombre_archivo)

        # abrimos la carpeta de la ruta del archivo
        with open(ruta_reporte, "w", newline="",encoding="utf-8-sig") as archivo:
            writer = csv.writer(archivo, delimiter=';')

            writer.writerow([
                "Usuario",
                "Anexo",
                "Proveedor",
                "Tipo de Llamada",
                "Duración Total (segundos)",
                "Cantidad de Llamadas del Usuario",
                "Cantidad Total de Llamadas",
                "Tiempo Total de la Carga (segundos)",
                "Promedio de Duración (segundos)",
                "Costo Calculado",
                "Fecha de Proceso"
            ])

            for reporte in reportes:
                writer.writerow([
                    reporte.id_usuario.nombre_usuario,
                    reporte.anexo,
                    reporte.proveedor,
                    reporte.id_tipo_llamada.nombre,
                    reporte.duracion_total_segundos,
                    reporte.cant_llamadas_usuario,
                    reporte.cant_total_llamadas_usuario,
                    reporte.total_tiempo_carga,
                    reporte.prom_duracion_llamada,
                    reporte.costo_calculado,
                    reporte.fecha_proceso
                ])

        self.rutaReporte = ruta_reporte

        return ruta_reporte