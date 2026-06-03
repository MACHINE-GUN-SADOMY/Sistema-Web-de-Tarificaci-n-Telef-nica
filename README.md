# AnexoControl / APT — Sistema Web de Tarificacion Telefonica

Sistema web academico para la carga, procesamiento y tarificacion de registros de llamadas telefonicas corporativas. Permite a usuarios cargar archivos CSV/TXT con registros de llamadas, procesarlos automaticamente y descargar reportes de tarificacion en formato PDF o CSV.

---

## Descripcion general

AnexoControl automatiza el proceso de auditoria de costos telefonicos dentro de una empresa. Cada llamada realizada desde un anexo interno queda registrada en un archivo de exportacion de la central telefonica. El sistema permite subir ese archivo, validar su estructura, limpiar los datos, calcular los costos de cada llamada mediante reglas almacenadas en base de datos, y generar un reporte descargable para el area de administracion.

El sistema diferencia dos tipos de usuario: administradores, que tienen acceso a metricas globales y gestion de cuentas, y empleados, que pueden crear solicitudes y ver sus propios reportes. El flujo completo desde la carga del archivo hasta el reporte descargable es automatico y no requiere intervencion manual intermedia.

---

## Tecnologias utilizadas

### Backend principal
- Java 17
- Spring Boot
- Spring MVC / REST
- JPA / Hibernate
- Thymeleaf (motor de plantillas para el frontend)

### Procesamiento
- Python
- Django + Django REST Framework
- pandas (lectura, limpieza y normalizacion de archivos)
- ReportLab (generacion de PDF)

### Base de datos
- Oracle XE
- Procedimientos almacenados (`PKG_TARIFICACION.PR_TARIFICAR_CARGA`)
- Funciones (`FN_CALCULAR_COSTO`)
- Secuencias
- Triggers

### Frontend
- HTML / CSS puro
- Google Material Symbols Outlined
- Fuentes: Geist y JetBrains Mono (Google Fonts)
- Sin JavaScript ni frameworks CSS externos

### Herramientas y pruebas
- Maven
- Postman (colecciones de pruebas incluidas)
- Git / GitHub
- JUnit 5 + Mockito (pruebas unitarias del modulo Spring)

---

## Arquitectura general

El sistema esta compuesto por dos modulos complementarios y una base de datos compartida:

```
Usuario (navegador)
        |
        v
Spring Boot (puerto 8081)
  Backend principal, sesion, frontend Thymeleaf
        |
        | HTTP POST /procesar-archivo-listo/
        v
Django / Python (puerto 8000)
  Procesamiento, tarificacion, generacion de reportes
        |
        | HTTP POST /procesamiento/callback (estado LISTO o ERROR)
        v
Spring Boot
  Actualiza estado de solicitud y sirve descarga
        |
        v
Usuario descarga el reporte PDF o CSV
```

Ambos modulos comparten la misma instancia Oracle. La coordinacion entre ellos se realiza principalmente mediante `id_carga`, que identifica unicamente cada grupo de registros procesados.

> El sistema no es una arquitectura de microservicios. Es un MVP con dos modulos complementarios que se comunican via HTTP.

---

## Flujo principal

1. El usuario inicia sesion en `http://localhost:8081/login`.
2. El usuario accede a la pantalla de solicitudes, selecciona el tipo de reporte y sube un archivo `.csv` o `.txt`.
3. Spring guarda el archivo en disco, crea una solicitud en estado `PENDIENTE` y asigna un `id_carga` unico.
4. Spring notifica a Django enviando los identificadores y la ruta del archivo.
5. Django valida la integridad referencial, lee el archivo con pandas, valida su estructura y limpia los datos.
6. Si el archivo tiene mas de 100 registros limpios, Django notifica a Spring con estado `ERROR` y detiene el proceso.
7. Django normaliza los datos e inserta los registros en Oracle mediante insercion masiva (`bulk_create`).
8. Django invoca el procedimiento almacenado `PKG_TARIFICACION.PR_TARIFICAR_CARGA` que calcula costos, duraciones y estadisticas.
9. Django genera el reporte PDF o CSV con los datos tarificados y lo guarda en disco.
10. Django notifica a Spring con estado `LISTO` y la ruta del reporte.
11. Spring actualiza el estado de la solicitud y habilita la descarga.
12. El usuario descarga el reporte desde la interfaz web.

---

## Funcionalidades del MVP

- Autenticacion de usuarios con roles (administrador y empleado).
- Registro de nuevos usuarios.
- Creacion de solicitudes de reporte con carga de archivo.
- Validacion de estructura del archivo (columnas requeridas, formato).
- Limite de 100 registros limpios por archivo.
- Procesamiento automatico: limpieza, normalizacion e insercion en Oracle.
- Tarificacion mediante procedimiento almacenado Oracle.
- Generacion de reportes en PDF (ReportLab) y CSV.
- Dashboard diferenciado por rol (admin ve metricas globales, empleado ve sus propias solicitudes).
- Descarga de reportes generados.
- Manejo visual de errores (pantalla de error Thymeleaf, sin JSON plano al usuario).
- Endpoints REST documentados para pruebas con Postman.
- 23 pruebas unitarias con JUnit 5 y Mockito en el modulo Spring.

---

## Roles del sistema

| Rol | Permisos principales |
|---|---|
| Administrador | Metricas globales del sistema, gestion de usuarios (crear, modificar, bloquear), ver y descargar cualquier solicitud |
| Empleado | Crear solicitudes propias, ver historial propio, descargar sus propios reportes |

---

## Estructura del proyecto

```
AnexoControl/
|
|-- Modulo_Spring/             Backend principal Spring Boot
|   |-- src/
|   |   |-- main/java/         Controladores, servicios, repositorios, entidades
|   |   |-- main/resources/    Plantillas Thymeleaf, archivos CSS, configuracion
|   |   |-- test/              Pruebas JUnit 5 + Mockito (23 pruebas)
|   |-- pom.xml
|   |-- Postman Collection/    Colecciones para pruebas manuales de endpoints
|
|-- Modulo_Django/             Modulo de procesamiento Python
|   |-- anexocontrol_django/   Configuracion del proyecto Django
|   |-- procesamiento/         Aplicacion principal
|   |   |-- api.py             Endpoints REST (APIView)
|   |   |-- service.py         Coordinador del flujo de procesamiento
|   |   |-- ProcesadorLlamadasService.py  Lectura, limpieza, normalizacion
|   |   |-- GeneradorReportesService.py   Generacion de PDF y CSV
|   |   |-- SpringCallbackService.py      Callback HTTP hacia Spring
|   |   |-- orm.py             Capa de acceso a datos (repositorio)
|   |   |-- models.py          Modelos ORM (managed=False sobre tablas Oracle)
|   |   |-- serializers.py     DTOs de entrada y salida
|   |-- requirements.txt
|
|-- Base_Datos/                Scripts SQL Oracle
|   |-- tablas, secuencias, triggers, funciones, procedimientos
|
|-- docs/                      Documentacion tecnica Markdown
```

---

## Ejecucion en entorno local

### Requisitos previos

- Java 17+
- Python 3.10+ con entorno virtual
- Oracle XE instalado y configurado localmente
- Maven
- Driver Oracle para Python (`cx_Oracle` u `oracledb`)

### 1. Base de datos

Ejecutar los scripts SQL del directorio `Base_Datos/` en orden sobre el schema `TARIFICACION` para crear tablas, secuencias, funciones y procedimientos.

### 2. Modulo Spring Boot

```bash
cd Modulo_Spring
# Configurar conexion Oracle en src/main/resources/application.properties
mvn spring-boot:run
# Disponible en http://localhost:8081
```

### 3. Modulo Django/Python

```bash
cd Modulo_Django
python -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
# Configurar conexion Oracle en anexocontrol_django/settings.py
python manage.py runserver 8000
# Disponible en http://localhost:8000
```

> Ambos modulos deben estar corriendo simultaneamente para que el flujo completo funcione.

### Orden de inicio recomendado

1. Oracle XE
2. Django (`python manage.py runserver 8000`)
3. Spring Boot (`mvn spring-boot:run`)
4. Abrir `http://localhost:8081/login` en el navegador

---

## Pruebas

### Pruebas automatizadas (Spring Boot)

```bash
cd Modulo_Spring
mvn test
```

23 pruebas unitarias con JUnit 5 y Mockito. No requieren conexion a Oracle ni a Django real.

| Clase de test | Pruebas |
|---|---|
| `ViewControllerTest` | 4 |
| `SolicitudReporteServiceTest` | 3 |
| `SolicitudReporteControllerTest` | 6 |
| `ProcesamientoCallbackControllerTest` | 2 |
| `UsuarioControllerTest` | 7 |
| `AnexocontrolApplicationTests` | 1 |

### Pruebas manuales (Postman)

Las colecciones de Postman estan en `Modulo_Spring/Postman Collection/` e incluyen endpoints de Spring y Django, junto con archivos CSV de prueba validos e invalidos.

### Pruebas Django

El modulo Django no tiene pruebas automatizadas implementadas en esta version del MVP. La verificacion se realizo mediante revision de codigo y pruebas manuales con Postman.

---

## Endpoints principales

### Spring Boot — REST
| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/usuario/login` | Autenticar usuario |
| POST | `/usuario/registrar-usuario` | Registrar nuevo usuario |
| GET | `/usuario/mostrar-usuarios` | Listar usuarios (admin) |
| POST | `/solicitud-reporte/crear-solicitud` | Crear solicitud con archivo |
| GET | `/solicitud-reporte/{id}/descargar` | Descargar reporte via API |
| POST | `/procesamiento/callback` | Recibir estado desde Django |

### Spring Boot — Web (Thymeleaf)
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/login` | Pantalla de login |
| GET | `/dashboard` | Dashboard segun rol |
| GET | `/reportes` | Crear solicitud y ver historial |
| GET | `/web/descargar/{id}` | Descarga con manejo de error visual |
| GET | `/usuarios` | Gestion de usuarios (admin) |

### Django — REST
| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/procesar-archivo-listo/` | Iniciar pipeline de procesamiento |
| GET | `/mostrar-tarificaciones/` | Listar reportes tarificados |
| GET | `/mostrar-tarificacion/<id>/` | Ver reporte por ID |
| DELETE | `/eliminar-tarificacion/<id>/` | Eliminar reporte |

---

## Estados de solicitud

| Estado | Descripcion |
|---|---|
| `PENDIENTE` | Solicitud creada, archivo guardado, esperando procesamiento de Django |
| `LISTO` | Procesamiento exitoso, reporte disponible para descarga |
| `ERROR` | Procesamiento fallido (archivo invalido, supera 100 registros u otro error controlado) |

---

## Limitaciones academicas

- Las contrasenas no estan hasheadas (sin BCrypt).
- Sin autenticacion segura en los endpoints Django (no hay tokens ni restriccion de IP).
- Limite de 100 registros limpios por archivo (restriccion del MVP).
- Sin paginacion en listados de solicitudes y usuarios.
- Sin notificaciones en tiempo real (el usuario debe recargar para ver el estado actualizado).
- Dependencia de entorno local: Spring, Django y Oracle deben correr en la misma maquina.
- El sistema no se conecta a centrales telefonicas reales; los archivos son generados o simulados manualmente.
- Si ocurre una excepcion inesperada en Django despues de las validaciones, Spring no recibe callback y la solicitud queda en estado `PENDIENTE` indefinidamente.

---

## Mejoras futuras

- Hasheo de contrasenas con BCrypt.
- Autenticacion segura en endpoints Django (token o restriccion por IP).
- Paginacion y filtros en el historial de solicitudes.
- Configuracion de credenciales y parametros sensibles mediante variables de entorno.
- Procesamiento asincrono con cola de mensajes (RabbitMQ o similar).
- Notificaciones en tiempo real con WebSocket o Server-Sent Events.
- Pruebas automatizadas para el modulo Django con pytest-django.
- Limite de registros configurable via variable de entorno.
- Integracion con APIs de proveedores de telefonia reales.
- Dockerizacion del entorno completo.

---

## Informacion academica

**Proyecto:** AnexoControl / APT — Sistema Web de Tarificacion Telefonica
**Contexto:** Proyecto academico — Fase 2 MVP
**Institucion:** Duoc UC
**Autor:** Kristian Hernandez
