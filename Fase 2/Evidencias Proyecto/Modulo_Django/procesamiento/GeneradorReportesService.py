import os
import csv


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

        # landscape(letter) = 792 x 612 pts
        # margen 36pt cada lado → ancho util = 720pt
        # colWidths suma exacta: 70+40+62+72+58+58+58+62+58+62+70 = 670 (holgura para padding)
        col_widths = [70, 40, 62, 72, 58, 58, 58, 62, 58, 62, 70]

        # se crea el documento con margenes reducidos para maximizar espacio de tabla
        documento = SimpleDocTemplate(
            ruta_reporte,
            pagesize=landscape(letter),
            leftMargin=36,
            rightMargin=36,
            topMargin=36,
            bottomMargin=36
        )

        # estilos para wrap de texto dentro de celdas
        estilos = getSampleStyleSheet()
        estilo_celda = estilos["Normal"]
        estilo_celda.fontSize = 7
        estilo_celda.leading = 9    # interlineado compacto
        estilo_celda.wordWrap = "LTR"

        estilo_header = estilos["Normal"].clone("header_cell")
        estilo_header.fontSize = 7
        estilo_header.leading = 9
        estilo_header.fontName = "Helvetica-Bold"
        estilo_header.wordWrap = "LTR"

        elementos = []

        # titulo del reporte
        titulo = Paragraph(f"Reporte de Tarificación - Carga {idCarga}", estilos["Title"])
        elementos.append(titulo)
        elementos.append(Spacer(1, 10))

        # encabezados con Paragraph para permitir wrap
        # nombres cortos para que quepan en las columnas definidas
        encabezados = [
            "Usuario",
            "Anexo",
            "Proveedor",
            "Tipo Llamada",
            "Dur. Total (s)",
            "Cant. Llam. Usuario",
            "Cant. Total Llam.",
            "T. Total Carga (s)",
            "Prom. Dur. (s)",
            "Costo",
            "Fecha Proceso"
        ]
        datos_tabla = [[Paragraph(h, estilo_header) for h in encabezados]]

        # filas de datos con Paragraph para permitir wrap en celdas largas
        for reporte in reportes:
            datos_tabla.append([
                Paragraph(str(reporte.id_usuario.nombre_usuario), estilo_celda),
                Paragraph(str(reporte.anexo),                     estilo_celda),
                Paragraph(str(reporte.proveedor),                 estilo_celda),
                Paragraph(str(reporte.id_tipo_llamada.nombre),    estilo_celda),
                Paragraph(str(reporte.duracion_total_segundos),   estilo_celda),
                Paragraph(str(reporte.cant_llamadas_usuario),     estilo_celda),
                Paragraph(str(reporte.cant_total_llamadas_usuario), estilo_celda),
                Paragraph(str(reporte.total_tiempo_carga),        estilo_celda),
                Paragraph(str(reporte.prom_duracion_llamada),     estilo_celda),
                Paragraph(str(reporte.costo_calculado),           estilo_celda),
                Paragraph(str(reporte.fecha_proceso),             estilo_celda),
            ])

        # tabla con anchos explícitos — evita desbordamiento
        tabla = Table(datos_tabla, colWidths=col_widths, repeatRows=1)

        tabla.setStyle(TableStyle([
            # encabezado
            ("BACKGROUND",    (0, 0), (-1, 0),  colors.HexColor("#2d3748")),
            ("TEXTCOLOR",     (0, 0), (-1, 0),  colors.white),
            ("FONTNAME",      (0, 0), (-1, 0),  "Helvetica-Bold"),
            ("FONTSIZE",      (0, 0), (-1, 0),  7),
            ("BOTTOMPADDING", (0, 0), (-1, 0),  6),
            ("TOPPADDING",    (0, 0), (-1, 0),  6),
            # filas de datos
            ("FONTNAME",      (0, 1), (-1, -1), "Helvetica"),
            ("FONTSIZE",      (0, 1), (-1, -1), 7),
            ("TOPPADDING",    (0, 1), (-1, -1), 4),
            ("BOTTOMPADDING", (0, 1), (-1, -1), 4),
            # alineacion
            ("ALIGN",         (0, 0), (-1, -1), "CENTER"),
            ("VALIGN",        (0, 0), (-1, -1), "MIDDLE"),
            # filas alternas para legibilidad
            ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#f7fafc")]),
            # grilla
            ("GRID",          (0, 0), (-1, -1), 0.4, colors.HexColor("#cbd5e0")),
            # borde exterior más marcado
            ("BOX",           (0, 0), (-1, -1), 0.8, colors.HexColor("#718096")),
        ]))

        elementos.append(tabla)

        # se construye el pdf final
        documento.build(elementos)

        self.rutaReporte = ruta_reporte

        # luego retorna la ubicacion del reporte
        return ruta_reporte