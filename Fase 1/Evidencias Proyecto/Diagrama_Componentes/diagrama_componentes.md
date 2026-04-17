@startuml
title Diagrama de Componentes - Sistema Web de Tarificación Telefónica

skinparam backgroundColor #F8FAFC
skinparam componentStyle rectangle
skinparam shadowing false
skinparam packageStyle rectangle

skinparam component {
    BackgroundColor #DBEAFE
    BorderColor #2563EB
    FontColor #111827
}

skinparam database {
    BackgroundColor #FEF3C7
    BorderColor #D97706
    FontColor #111827
}

skinparam folder {
    BackgroundColor #DCFCE7
    BorderColor #16A34A
    FontColor #111827
}

skinparam node {
    BackgroundColor #EDE9FE
    BorderColor #7C3AED
    FontColor #111827
}

actor "Empleado" as Empleado
actor "Administrador" as Admin

node "Sistema Principal\nSpring Boot" as SpringApp {

    component "«UI»\nFrontend\n(Thymeleaf / HTML / CSS)" as Frontend

    package "Módulo Autenticación y Cuentas" as ModAuth {
        component "UsuarioController" as UsuarioController
        component "UsuarioService" as UsuarioService
        component "UsuarioRepository\n«JpaRepository»" as UsuarioRepository
        component "RolRepository\n«JpaRepository»" as RolRepository
    }

    package "Módulo Importación" as ModImport {
        component "ArchivoImportadoController" as ArchivoController
        component "ArchivoImportadoService" as ArchivoService
        component "ArchivoImportadoRepository\n«JpaRepository»" as ArchivoRepository
    }

    package "Módulo Solicitud de Reportes" as ModReportSpring {
        component "SolicitudReporteController" as SolicitudController
        component "SolicitudReporteService" as SolicitudService
        component "SolicitudReporteRepository\n«JpaRepository»" as SolicitudRepository
    }

    component "«framework»\nJPA / Hibernate" as JPA
}

node "Procesamiento Batch\nPython + Pandas" as PythonNode {
    component "FileWatcher" as FileWatcher
    component "GestorProcesamiento" as GestorProcesamiento
    component "ProcesadorLlamadas" as ProcesadorLlamadas
    component "Tarificador" as Tarificador
    component "DatabaseConnector" as PyDB
}

node "Servicio de Reportes\nDjango REST" as DjangoNode {
    package "Módulo Reportes" as ModDjango {
        component "ReporteListaAPIView" as ReporteListaAPIView
        component "ReporteDetalleAPIView" as ReporteDetalleAPIView
        component "ReporteDescargaAPIView" as ReporteDescargaAPIView
        component "ReporteService" as ReporteService
        component "ReporteRepositoryORM" as ReporteRepositoryORM
        component "ReporteListaSerializer" as ReporteListaSerializer
        component "ReporteDetalleSerializer" as ReporteDetalleSerializer
        component "ReporteTarificacion\n«Model»" as ReporteTarificacion
    }
}

database "Base de Datos Relacional\nMySQL" as MySQL

folder "/archivos/pendientes" as Pendientes
folder "/archivos/procesados" as Procesados
folder "/archivos/reportes" as ReportesDir

' Actores hacia frontend
Empleado --> Frontend : Usa sistema
Admin --> Frontend : Administra sistema

' Flujo UI -> Spring
Frontend --> UsuarioController : Registro / Login / Logout
Frontend --> ArchivoController : Subir TXT / CSV
Frontend --> SolicitudController : Solicitar / Descargar reporte

' Flujo interno Spring
UsuarioController --> UsuarioService
UsuarioService --> UsuarioRepository
UsuarioService --> RolRepository

ArchivoController --> ArchivoService
ArchivoService --> ArchivoRepository

SolicitudController --> SolicitudService
SolicitudService --> SolicitudRepository

UsuarioRepository --> JPA
RolRepository --> JPA
ArchivoRepository --> JPA
SolicitudRepository --> JPA
JPA --> MySQL : Lectura / Escritura SQL

' Intercambio por carpetas compartidas
ArchivoService --> Pendientes : Guarda archivo importado
FileWatcher --> Pendientes : Monitorea y toma archivo

GestorProcesamiento --> ProcesadorLlamadas : Orquesta lectura / limpieza
GestorProcesamiento --> Tarificador : Ejecuta cálculo
GestorProcesamiento --> PyDB : Inserta resultados
ProcesadorLlamadas --> Procesados : Mueve archivo procesado
PyDB --> MySQL : Inserta datos limpios / procesados

' Django consulta BD y genera reportes
ReporteListaAPIView --> ReporteService
ReporteDetalleAPIView --> ReporteService
ReporteDescargaAPIView --> ReporteService

ReporteService --> ReporteRepositoryORM
ReporteService --> ReporteListaSerializer
ReporteService --> ReporteDetalleSerializer

ReporteRepositoryORM --> ReporteTarificacion
ReporteTarificacion --> MySQL : Consulta ORM
ReporteService --> ReportesDir : Genera PDF / CSV

' Spring solicita/ubica el reporte generado
SolicitudService --> DjangoNode : Solicita reporte / consulta disponibilidad
SolicitudService --> ReportesDir : Busca archivo generado
SolicitudController --> Frontend : Entrega descarga

@enduml