@startuml
title Diagrama de Paquetes - Sistema Web de Tarificación Telefónica
skinparam backgroundColor #F8FAFC
skinparam packageStyle rectangle
skinparam shadowing false
skinparam defaultFontName Arial
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

package "Presentation Layer" as presentation {
    package "Frontend\n(Thymeleaf / HTML / CSS)" as frontend
    package "Controladores Spring" as controllers {
        package "ctrl_auth\nAutenticación y Cuentas" as ctrl_auth
        package "ctrl_import\nImportación" as ctrl_import
        package "ctrl_report\nDescarga de Reportes" as ctrl_report
    }
}

package "Business Layer" as business {
    package "Servicios Spring" as spring_services {
        package "srv_auth\nAutenticación y Cuentas" as srv_auth
        package "srv_import\nImportación" as srv_import
        package "srv_report\nRedirección de Reportes" as srv_report
    }
    package "Procesamiento Batch Python" as python_pkg {
        package "py_watch\nMonitoreo de Archivos" as py_watch
        package "py_process\nProcesamiento con Pandas" as py_process
        package "py_rate\nTarificación" as py_rate
        package "py_db\nConexión a Base de Datos" as py_db
    }
    package "Servicios Django REST" as django_services {
        package "dj_api\nSerializers / APIView" as dj_api
        package "dj_query\nConsulta de Reportes" as dj_query
        package "dj_export\nGeneración PDF / CSV" as dj_export
    }
}

package "Data Layer" as data {
    package "Repositorios Spring (JPA)" as spring_repo {
        package "repo_auth\nUsuarioRepository / RolRepository" as repo_auth
        package "repo_import\nArchivoImportadoRepository" as repo_import
        package "repo_report\nSolicitudReporteRepository" as repo_report
    }
    package "ORM Django" as django_repo {
        package "repo_django\nReporteRepositoryORM" as repo_django
        package "model_django\nReporteTarificacion (Model)" as model_django
    }
    package "Persistencia y Recursos" as persistence {
        database "MySQL" as mysql
        package "Carpetas Compartidas" as folders {
            package "pendientes\n/archivos/pendientes" as pendientes
            package "procesados\n/archivos/procesados" as procesados
            package "reportes\n/archivos/reportes" as reportes
        }
    }
}

' Presentation -> Business
frontend ..> ctrl_auth : HTTP
frontend ..> ctrl_import : HTTP
frontend ..> ctrl_report : HTTP

ctrl_auth ..> srv_auth
ctrl_import ..> srv_import
ctrl_report ..> srv_report

' Spring Services -> Repositories
srv_auth ..> repo_auth
srv_import ..> repo_import
srv_report ..> repo_report

' Spring -> Carpetas
srv_import ..> pendientes : guarda archivo
srv_report ..> reportes : obtiene reporte

' Spring Repositories -> MySQL
repo_auth ..> mysql
repo_import ..> mysql
repo_report ..> mysql

' Python Batch
py_watch ..> pendientes : monitorea
py_watch ..> py_process : entrega archivo
py_process ..> py_rate : aplica reglas
py_process ..> py_db : inserta resultados
py_process ..> procesados : mueve archivo
py_db ..> mysql

' Spring redirige a Django para generar reporte
srv_report ..> dj_api : solicita reporte

' Django REST
dj_api ..> dj_query
dj_api ..> dj_export
dj_query ..> repo_django
dj_export ..> repo_django
repo_django ..> model_django
model_django ..> mysql
dj_export ..> reportes : genera PDF/CSV

@enduml