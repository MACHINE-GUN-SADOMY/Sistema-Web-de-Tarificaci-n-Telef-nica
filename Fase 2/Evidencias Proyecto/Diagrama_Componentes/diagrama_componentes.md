@startuml
title Diagrama de Componentes - Sistema Web de Tarificación Telefónica

skinparam backgroundColor #F8FAFC
skinparam componentStyle rectangle
skinparam shadowing false
skinparam packageStyle rectangle
skinparam defaultFontName Arial
left to right direction

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
skinparam interface {
    BackgroundColor #FFFFFF
    BorderColor #2563EB
}

node "Servidor Principal" as MainNode {

  package "Frontend" as FrontPkg {
    component "Thymeleaf / HTML / CSS" as Frontend
  }

  node "Spring Boot" as SpringNode {

    package "Autenticación y Cuentas" as ModAuth {
      component "UsuarioController" as UsuarioController
      component "UsuarioService" as UsuarioService
      component "UsuarioJpa" as UsuarioJpa
      component "RolJpa" as RolJpa
      interface "UsuarioJpaRepository" as UsuarioRepo
      interface "RolJpaRepository" as RolRepo
      component "UsuarioRegistroDTO" as UsuarioRegistroDTO
      component "UsuarioLoginDTO" as UsuarioLoginDTO
      component "UsuarioRespuestaDTO" as UsuarioRespuestaDTO
    }

    package "Importación" as ModImport {
      component "ArchivoImportadoController" as ArchivoController
      component "ArchivoImportadoService" as ArchivoService
      component "ArchivoImportadoDTO" as ArchivoDTO
      component "ProcesamientoCallbackController" as ProcCallbackController
    }

    package "Reportes Spring" as ModReportSpring {
      component "SolicitudReporteController" as SolicitudController
      component "SolicitudReporteService" as SolicitudService
      component "SolicitudReporteJpa" as SolicitudJpa
      interface "SolicitudReporteJpaRepository" as SolicitudRepo
      component "SolicitudReporteDTO" as SolicitudDTO
      component "ReporteRespuestaDTO" as ReporteRespuestaDTO
      component "ReporteCallbackController" as ReporteCallbackController
    }

    component "JPA / Hibernate" as JPA
  }

  node "Procesamiento Python" as PythonNode {
    component "ProcesamientoAPIView" as ProcesamientoAPIView
    component "ProcesadorLlamadasService" as ProcesadorService
  }

  node "Reportes Django REST" as DjangoNode {

    package "APIViews" as DjangoViews {
      component "ReporteListaAPIView" as ReporteListaAPIView
      component "ReporteDetalleAPIView" as ReporteDetalleAPIView
      component "ReporteDescargaAPIView" as ReporteDescargaAPIView
    }

    package "Servicios" as DjangoSrv {
      component "ReporteService" as ReporteService
    }

    package "Repositorio y Modelo" as DjangoData {
      component "ReporteRepositoryORM" as ReporteRepositoryORM
      component "ReporteTarificacion\n«Model / managed=False»" as ReporteTarificacion
    }

    package "Serializers" as DjangoSerial {
      component "ReporteListaSerializer" as ReporteListaSerializer
      component "ReporteDetalleSerializer" as ReporteDetalleSerializer
    }
  }

  database "MySQL" as MySQL
  folder "/archivos/pendientes" as Pendientes
  folder "/archivos/reportes" as ReportesDir
}

' ─── Frontend → Spring ───
Frontend -[#2563EB,thickness=2]-> UsuarioController    : <<HTTPS>> Registro / Login / Cuenta
Frontend -[#2563EB,thickness=2]-> ArchivoController    : <<HTTPS>> Subir TXT / CSV
Frontend -[#2563EB,thickness=2]-> SolicitudController  : <<HTTPS>> Solicitar / Descargar reporte

' ─── Autenticación interno ───
UsuarioController ..> UsuarioRegistroDTO
UsuarioController ..> UsuarioLoginDTO
UsuarioController ..> UsuarioRespuestaDTO
UsuarioController -[#7C3AED]-> UsuarioService
UsuarioService -[#7C3AED]-> UsuarioRepo
UsuarioService -[#7C3AED]-> RolRepo
UsuarioRepo -[#0F766E]-> UsuarioJpa
RolRepo    -[#0F766E]-> RolJpa
UsuarioRepo -[#0F766E]-> JPA
RolRepo     -[#0F766E]-> JPA

' ─── Importación interno ───
ArchivoController ..> ArchivoDTO
ArchivoController -[#7C3AED]-> ArchivoService
ArchivoService -[#D97706,dashed]-> Pendientes           : <<FileSystem Write>>
ArchivoService -[#DC2626,thickness=2]-> ProcesamientoAPIView : <<HTTP POST>>
ProcCallbackController -[#7C3AED]-> ArchivoService      : callback procesamiento

' ─── Reportes Spring interno ───
SolicitudController ..> SolicitudDTO
SolicitudController ..> ReporteRespuestaDTO
SolicitudController -[#7C3AED]-> SolicitudService
SolicitudService -[#7C3AED]-> SolicitudRepo
SolicitudRepo -[#0F766E]-> SolicitudJpa
SolicitudRepo -[#0F766E]-> JPA
SolicitudService -[#DC2626,thickness=2]-> ReporteListaAPIView    : <<HTTP GET>>
SolicitudService -[#DC2626,thickness=2]-> ReporteDetalleAPIView  : <<HTTP GET>>
SolicitudService -[#DC2626,thickness=2]-> ReporteDescargaAPIView : <<HTTP GET>>
ReporteCallbackController -[#7C3AED]-> SolicitudService : callback reporte listo
SolicitudService -[#D97706,dashed]-> ReportesDir         : <<FileSystem Read>>

' ─── JPA → MySQL ───
JPA -[#CA6F1E,thickness=2]-> MySQL : <<JPA / Hibernate>>

' ─── Python interno ───
ProcesamientoAPIView -[#7C3AED]-> ProcesadorService
ProcesadorService -[#0F766E]-> ReporteRepositoryORM : <<SQLAlchemy>>

' ─── Django APIViews → Service ───
ReporteListaAPIView    -[#7C3AED]-> ReporteService
ReporteDetalleAPIView  -[#7C3AED]-> ReporteService
ReporteDescargaAPIView -[#7C3AED]-> ReporteService

' ─── Django APIViews → Serializers ───
ReporteListaAPIView   ..> ReporteListaSerializer
ReporteDetalleAPIView ..> ReporteDetalleSerializer

' ─── Django Service → Datos ───
ReporteService -[#0F766E]-> ReporteRepositoryORM
ReporteService ..> ReporteListaSerializer
ReporteService ..> ReporteDetalleSerializer
ReporteService -[#D97706,dashed]-> ReportesDir : <<FileSystem Write>>

' ─── ORM → Model → MySQL ───
ReporteRepositoryORM  -[#0F766E]-> ReporteTarificacion
ReporteTarificacion   -[#CA6F1E,thickness=2]-> MySQL : <<Django ORM>>

@enduml