@startuml
title Diagrama de Paquetes - Sistema Web de Tarificación Telefónica
skinparam backgroundColor #F8FAFC
skinparam packageStyle rectangle
skinparam shadowing false
skinparam defaultFontName Arial
left to right direction

skinparam package {
    BackgroundColor #DBEAFE
    BorderColor #2563EB
    FontColor #111827
}
skinparam database {
    BackgroundColor #FEF3C7
    BorderColor #D97706
    FontColor #111827
}
skinparam arrow {
    Color #1E40AF
    FontColor #1E40AF
    FontSize 9
}

package "Presentation Layer" as presentation {
  package "Frontend\n(Thymeleaf / HTML / CSS)" as frontend
  package "Spring Controllers" as spring_ctrl {
    package "ctrl_auth\nAutenticación y Cuentas" as ctrl_auth
    package "ctrl_import\nImportación" as ctrl_import
    package "ctrl_report\nSolicitud de Reportes" as ctrl_report
    package "ctrl_callback\nCallbacks" as ctrl_callback
  }
  package "Django APIViews" as django_api {
    package "api_process\nProcesamientoAPIView" as api_process
    package "api_list\nReporteListaAPIView" as api_list
    package "api_detail\nReporteDetalleAPIView" as api_detail
    package "api_download\nReporteDescargaAPIView" as api_download
  }
}

package "Business Layer" as business {
  package "Spring Services" as spring_srv {
    package "srv_auth\nUsuarioService" as srv_auth
    package "srv_import\nArchivoImportadoService" as srv_import
    package "srv_report\nSolicitudReporteService" as srv_report
  }
  package "Python Processing" as python_business {
    package "py_process\nProcesadorLlamadasService" as py_process
  }
  package "Django Services" as django_srv {
    package "dj_service\nReporteService" as dj_service
  }
}

package "Data Layer" as data {
  package "Spring Repositories" as spring_repo {
    package "repo_auth\nUsuarioRepository / RolRepository" as repo_auth
    package "repo_report\nSolicitudReporteRepository" as repo_report
  }
  package "Django Data" as django_data {
    package "repo_orm\nReporteRepositoryORM" as repo_orm
    package "model_tar\nReporteTarificacion" as model_tar
    package "dto_serializers\nReporteListaSerializer\nReporteDetalleSerializer" as dto_serializers
  }
  package "Persistencia y Recursos" as persistence {
    database "MySQL" as mysql
    package "Carpetas Compartidas" as folders {
      package "pendientes\n/archivos/pendientes" as pendientes
      package "reportes\n/archivos/reportes" as reportes
    }
  }
}

' ─── HTTPS: Navegador → Spring ───
frontend -[#2563EB,thickness=2]-> ctrl_auth    : <<HTTPS>>
frontend -[#2563EB,thickness=2]-> ctrl_import  : <<HTTPS>>
frontend -[#2563EB,thickness=2]-> ctrl_report  : <<HTTPS>>

' ─── Controllers → Services ───
ctrl_auth     -[#7C3AED,thickness=1]-> srv_auth
ctrl_import   -[#7C3AED,thickness=1]-> srv_import
ctrl_report   -[#7C3AED,thickness=1]-> srv_report
ctrl_callback -[#7C3AED,thickness=1]-> srv_import
ctrl_callback -[#7C3AED,thickness=1]-> srv_report

' ─── Spring Services → Repositories (JPA) ───
srv_auth   -[#0F766E,thickness=1]-> repo_auth   : <<JPA>>
srv_report -[#0F766E,thickness=1]-> repo_report : <<JPA>>

' ─── Spring Services → FileSystem ───
srv_import -[#D97706,thickness=1,dashed]-> pendientes : <<FileSystem Write>>
srv_report -[#D97706,thickness=1,dashed]-> reportes   : <<FileSystem Read>>

' ─── Spring Services → Django APIViews (HTTP interno) ───
srv_import -[#DC2626,thickness=2]-> api_process  : <<HTTP POST>>
srv_report -[#DC2626,thickness=2]-> api_list     : <<HTTP GET>>
srv_report -[#DC2626,thickness=2]-> api_detail   : <<HTTP GET>>
srv_report -[#DC2626,thickness=2]-> api_download : <<HTTP GET>>

' ─── Django APIViews → Services ───
api_process  -[#7C3AED,thickness=1]-> py_process
api_list     -[#7C3AED,thickness=1]-> dj_service
api_detail   -[#7C3AED,thickness=1]-> dj_service
api_download -[#7C3AED,thickness=1]-> dj_service

' ─── Python → ORM ───
py_process -[#0F766E,thickness=1]-> repo_orm : <<SQLAlchemy>>

' ─── Django Service → Data ───
dj_service -[#0F766E,thickness=1]-> repo_orm
dj_service -[#0F766E,thickness=1]-> dto_serializers
dj_service -[#D97706,thickness=1,dashed]-> reportes : <<FileSystem Write>>

' ─── ORM → Model → MySQL ───
repo_orm  -[#0F766E,thickness=1]-> model_tar
model_tar -[#CA6F1E,thickness=2]-> mysql : <<Django ORM>>

' ─── Spring Repositories → MySQL ───
repo_auth   -[#CA6F1E,thickness=2]-> mysql : <<JPA / Hibernate>>
repo_report -[#CA6F1E,thickness=2]-> mysql : <<JPA / Hibernate>>

@enduml