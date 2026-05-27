import os
import csv

from django.utils.lorem_ipsum import paragraph
from rest_framework import serializers
from django.conf import settings
from rest_framework.exceptions import ValidationError

# imports de reportLab
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter, landscape
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer

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
            return self.generarReportePDF(reportes, idCarga)

        # si el tipo es incorrecto
        raise serializers.ValidationError("Tipo de reporte invalido.")

    # para generar los CSV
    def generarReporteCSV(self, reportes, idCarga: int):
        # se crea la carpeta en la base de la direccion
        carpeta_reportes = os.path.join(settings.ARCHIVOS_BASE_DIR, "reportes")
        os.makedirs(carpeta_reportes, exist_ok=True)

        # se pone el nombre al archivo
        nombre_archivo = f"reporte_carga_{idCarga}.csv"
        ruta_reporte = os.path.join(carpeta_reportes, nombre_archivo)

        # abrimos la carpeta de la ruta del archivo (w para escribir)
        with open(ruta_reporte, "w", newline="",encoding="utf-8-sig") as archivo:
            writer = csv.writer(archivo, delimiter=';') # el writer sera el objeto escritura
            # se le entregan los parametros, delimiter para escribir por columnas

            # el writer escribira por fila de encabezado
            # se ejecuta solo una vez para definir titulos de columna
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

            # luego pasara por cada uno de los reportes para escribir
            for reporte in reportes: # este sera por for
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
                ]) #

        self.rutaReporte = ruta_reporte

        # luego retorna la ubicacion del reporte
        return ruta_reporte

    # para generar los PDF
    def generarReportePDF(self, reportes, idCarga: int):
        # se crea la carpeta en la base de la direccion
        carpeta_reportes = os.path.join(settings.ARCHIVOS_BASE_DIR, "reportes")
        os.makedirs(carpeta_reportes, exist_ok=True)

        # se pone el nombre al archivo
        nombre_archivo = f"reporte_carga_{idCarga}.pdf"
        ruta_reporte = os.path.join(carpeta_reportes, nombre_archivo)

        # se crea el documento base
        documento = SimpleDocTemplate(
            ruta_reporte,
            pagesize=landscape(letter)
        )

        # se obtienen los estilos por defecto
        estilos = getSampleStyleSheet()
        elementos = []

        # se crea el titulo del reporte
        titulo = Paragraph(f"Reporte de Tarificación - Carga {idCarga}", estilos["Title"])

        elementos.append(titulo)
        elementos.append(Spacer(1,12))

        # se ejecuta solo una vez para definir titulos de columna
        datos_tabla = [
            [
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
            ]
        ]

        # luego pasara por cada uno de los reportes para escribir
        for reporte in reportes: # este sera por for
            datos_tabla.append([
                str(reporte.id_usuario.nombre_usuario),
                str(reporte.anexo),
                str(reporte.proveedor),
                str(reporte.id_tipo_llamada.nombre),
                str(reporte.duracion_total_segundos),
                str(reporte.cant_llamadas_usuario),
                str(reporte.cant_total_llamadas_usuario),
                str(reporte.total_tiempo_carga),
                str(reporte.prom_duracion_llamada),
                str(reporte.costo_calculado),
                str(reporte.fecha_proceso)
            ])

        # se crea el objeto de la tabla
        tabla = Table(datos_tabla, repeatRows=1)

        # se le entregan los parametros de estilos
        tabla.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.lightgrey),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.black),
            ("ALIGN", (0, 0), (-1, -1), "CENTER"),
            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
            ("FONTNAME", (0, 1), (-1, -1), "Helvetica"),
            ("FONTSIZE", (0, 0), (-1, 0), 8),
            ("FONTSIZE", (0, 1), (-1, -1), 7),
            ("BOTTOMPADDING", (0, 0), (-1, 0), 8),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
        ]))

        elementos.append(tabla)

        # se construye el pdf final
        documento.build(elementos)

        self.rutaReporte = ruta_reporte

        # luego retorna la ubicacion del reporte
        return ruta_reporte