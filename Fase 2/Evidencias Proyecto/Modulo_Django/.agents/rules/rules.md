---
trigger: model_decision
---

# Rules.md - AnexoControl / Sistema Web de Tarificacion Telefonica

## 1. Contexto general del proyecto
Este archivo define reglas de trabajo para Antigravity dentro del proyecto AnexoControl.
El sistema permite crear solicitudes de reporte telefonico adjuntando archivos CSV/TXT.
Spring Boot recibe la solicitud, guarda el archivo, genera idCarga, crea la solicitud y avisa a Django.
Django/Python procesa el archivo, inserta registros limpios, coordina la tarificacion, genera el reporte y avisa a Spring por callback.
Oracle es la base de datos principal y contiene tablas, secuencias, triggers y package de tarificacion.
No tratar este proyecto como microservicios complejos; es separacion por responsabilidades dentro de un mismo sistema.

## 2. Componentes principales
- Spring Boot: backend principal, usuarios, login, solicitudes, carga de archivos, callback y descarga.
- Oracle: base de datos principal, triggers, secuencias y PKG_TARIFICACION.
- Django/Python: procesamiento de archivos, limpieza, insercion de registros y generacion de reportes.
- Thymeleaf/HTML/CSS: frontend posterior dentro de Resources.
- Postman: pruebas de endpoints REST.
- GitHub: evidencias, commits y trazabilidad del desarrollo.

## 3. Reglas generales
- Mantener nombres de carpetas en PascalCase si ya existen asi.
- No renombrar paquetes, clases ni carpetas sin necesidad.
- No modificar la base de datos desde codigo salvo indicacion explicita.
- No usar ddl-auto=update, create o create-drop.
- Spring solo debe consumir la BD existente.
- Django solo debe mapear tablas Oracle existentes.
- No implementar JWT.
- No implementar Spring Security por ahora.
- No usar hashing de contrasenas por ahora.
- No exponer contrasenha en responses.
- No mostrar tarificacion en tiempo real en la pagina.
- No recalcular tarifas en Spring.
- No crear vistas visibles de registros internos salvo solicitud expresa.
- No crear microservicios nuevos.
- No crear carpeta Dev.
- No crear DashboardService, DashboardRepository ni DashboardJPA.
- Evitar comentarios largos.
- Si se agregan comentarios, deben ser breves y sin tildes.
- Priorizar codigo simple, claro y defendible academicamente.
- Respetar el flujo Controller -> Service -> Repository -> JPA.

## 4. Flujo principal del sistema
1. Usuario inicia sesion.
2. Usuario crea solicitud de reporte y adjunta archivo CSV/TXT.
3. Spring recibe la solicitud.
4. ArchivoService valida y guarda el archivo.
5. Spring genera idCarga con SEQ_CARGA.
6. Spring crea solicitud_reporte en estado PENDIENTE.
7. Spring usa ProcesamientoClient para avisar a Django que el archivo esta listo.
8. Django recibe idSolicitud, idCarga, idUsuario, idTipoReporte y rutaArchivo.
9. Django/Python procesa el archivo.
10. Django/Python inserta registros limpios en REGISTRO_LLAMADA.
11. Django/Python ejecuta o coordina la tarificacion con Oracle.
12. Django/Python genera el reporte final.
13. Django llama a Spring mediante POST /procesamiento/callback.
14. Spring actualiza la solicitud a LISTO o ERROR.
15. Usuario descarga el reporte desde Spring.

## 5. Spring Boot - estructura esperada
- src/main/java/cl/anexocontrol/Archivo
- src/main/java/cl/anexocontrol/Common
- src/main/java/cl/anexocontrol/ErrorProceso
- src/main/java/cl/anexocontrol/RegistroLlamada
- src/main/java/cl/anexocontrol/ReporteTarificacion
- src/main/java/cl/anexocontrol/Rol
- src/main/java/cl/anexocontrol/SolicitudReporte
- src/main/java/cl/anexocontrol/Usuario

## 6. Spring Boot - patron de capas
- Controller recibe request, llama al service y responde con ResponseEntity.
- Service contiene logica de negocio y validaciones.
- Repository accede a datos mediante JpaRepository.
- JPA representa tablas Oracle.
- DTO Request representa datos de entrada.
- DTO Response representa datos de salida.
- No devolver entidades directamente si ya existe response DTO.
- No poner logica fuerte en controllers.

## 7. Codigos HTTP
- 200 OK para consultas y actualizaciones correctas.
- 201 CREATED para creaciones correctas.
- 204 NO_CONTENT solo si corresponde.
- 400 BAD_REQUEST para errores de validacion o errores controlados.
- No complicar con muchos codigos HTTP salvo solicitud expresa.

## 8. Usuario
- El modulo Usuario centraliza login y gestion de cuentas.
- No recrear modulo Auth.
- El login debe quedar en POST /usuarios/login.
- Empleado solo puede modificar datos basicos de su cuenta.
- Admin puede modificar cualquier cuenta, rol y estado.
- DELETE de usuario debe funcionar como baja logica si hay FK asociadas.
- La baja logica se hace dejando estado_cuenta = INACTIVA.
- No eliminar fisicamente usuarios con registros asociados.
- CrearUsuarioRequest no debe recibir idUsuario.
- idUsuario se genera con SEQ_USUARIO.
- UsuarioResponse no debe incluir contrasena.

## 9. Rutas Usuario
- POST /usuarios
- POST /usuarios/login
- GET /usuarios/{idUsuario}
- GET /usuarios?idRolSolicitante=1
- PUT /usuarios/mi-cuenta/{idUsuario}
- PUT /usuarios/admin/{idUsuario}
- DELETE /usuarios/admin/{idUsuario}

## 10. SolicitudReporte
- SolicitudReporte es el centro del flujo de reportes.
- POST /solicitudes-reporte recibe multipart/form-data.
- El usuario crea la solicitud adjuntando archivo CSV/TXT.
- El request debe recibir archivo, idUsuario e idTipoReporte.
- El usuario no debe enviar idCarga.
- idCarga se genera internamente con SEQ_CARGA.
- La solicitud se crea en estado PENDIENTE.
- rutaReporte inicial debe ser PENDIENTE porque en Oracle es NOT NULL.
- No guardar null en rutaReporte.
- Solo se descarga si estado_solicitud = LISTO.
- Empleados solo pueden ver sus propias solicitudes.
- Administradores pueden ver todas las solicitudes.
- Mantener validacion por idUsuarioSolicitante e idRolSolicitante hasta que exista sesion real.

## 11. Rutas SolicitudReporte
- POST /solicitudes-reporte
- GET /solicitudes-reporte/{idSolicitud}
- GET /solicitudes-reporte/usuario/{idUsuario}
- GET /solicitudes-reporte/carga/{idCarga}
- PUT /solicitudes-reporte/{idSolicitud}/estado
- GET /solicitudes-reporte/{idSolicitud}/descargar

## 12. ArchivoService
- ArchivoService es apoyo interno para SolicitudReporte.
- Debe validar que el archivo exista.
- Debe validar extension .csv o .txt.
- Debe guardar fisicamente el archivo.
- Debe retornar la ruta local del archivo guardado.
- No debe crear solicitudes por si solo.
- No debe procesar el archivo.
- No debe insertar llamadas en la BD.
- No debe tarificar.

## 13. ProcesamientoClient
- ProcesamientoClient es el puente HTTP de Spring hacia Django.
- Debe construir y enviar la peticion HTTP a Django.
- Debe notificar que el archivo esta listo.
- Debe enviar idSolicitud, idCarga, idUsuario, idTipoReporte y rutaArchivo.
- La URL base de Django debe estar en application.properties si es posible.
- No confundir ProcesamientoClient con el callback.

## 14. ProcesamientoCallbackController
- ProcesamientoCallbackController recibe la respuesta de Django hacia Spring.
- Debe exponer POST /procesamiento/callback.
- Django llama este endpoint cuando termina el procesamiento.
- Debe actualizar estadoSolicitado a LISTO o ERROR.
- Debe actualizar rutaReporte con la ruta final del reporte.
- No debe llamar a Django.

## 15. Django/Python
- Django debe recibir la peticion desde Spring en /api/procesamiento/archivo-listo/.
- Django debe usar modelos ORM sobre tablas Oracle existentes.
- Todos los modelos ORM deben usar class Meta con managed = False.
- No usar def Meta(self).
- No usar def Meta.
- No permitir que Django cree tablas Oracle existentes.
- Los modelos deben mapear db_table y db_column correctamente.
- Las relaciones FK pueden usar ForeignKey con on_delete=models.DO_NOTHING.
- No usar max_length en IntegerField.
- Usar DecimalField para montos y promedios si tienen decimales.
- Usar __str__ con f-string cuando haya enteros.

## 16. Tablas Oracle principales
- ROL
- USUARIO
- TIPO_REPORTE
- TIPO_LLAMADA
- REGISTRO_LLAMADA
- REPORTE_TARIFICACION
- SOLICITUD_REPORTE
- ERROR_PROCESO

## 17. Secuencias Oracle
- SEQ_USUARIO
- SEQ_CARGA
- SEQ_SOLICITUD_REPORTE
- SEQ_REPORTE_TARIFICACION
- SEQ_ERROR_PROCESO

## 18. Estados de solicitud
- PENDIENTE
- GENERANDO
- LISTO
- ERROR

## 19. Restricciones importantes
- No mostrar al usuario final registros internos de llamadas salvo indicacion.
- No mostrar tarificacion en tiempo real dentro de la pagina.
- No recalcular tarifas en el frontend.
- No duplicar numero_destino en reporte_tarificacion si se puede obtener por JOIN.
- La generacion final del reporte la hace Django/Python.
- Oracle deja datos tarificados listos.
- Spring sirve la descarga cuando la solicitud queda LISTO.

## 20. Commits
- Usar commits en espanol y en tercera persona.
- Preferir estilo conventional commits.
- Ejemplo: feat(solicitud-reporte): implementa modulo y carga de archivos.
- No incluir archivos locales de agentes si estan en .gitignore.
- Revisar git status antes de commitear.

## 21. Reglas operativas extendidas
- Regla operativa 00001: mantener Controller -> Service -> Repository -> JPA.
- Regla operativa 00002: no modificar BD ni migraciones sin instruccion explicita.
- Regla operativa 00003: validar permisos de empleado y administrador.
- Regla operativa 00004: no exponer contrasenas ni datos internos innecesarios.
- Regla operativa 00005: usar estados PENDIENTE, GENERANDO, LISTO y ERROR.
- Regla operativa 00006: mantener ArchivoService solo para validar y guardar archivos.
- Regla operativa 00007: mantener ProcesamientoClient solo para avisar a Django.
- Regla operativa 00008: mantener CallbackController solo para recibir respuesta de Django.
- Regla operativa 00009: en Django usar class Meta con managed = False.
- Regla operativa 00010: no usar max_length en IntegerField.
- Regla operativa 00011: no mostrar tarificacion en tiempo real al usuario final.
- Regla operativa 00012: documentar cambios con commits claros en espanol.
- Regla operativa 00013: mantener Controller -> Service -> Repository -> JPA.
- Regla operativa 00014: no modificar BD ni migraciones sin instruccion explicita.
- Regla operativa 00015: validar permisos de empleado y administrador.
- Regla operativa 00016: no exponer contrasenas ni datos internos innecesarios.
- Regla operativa 00017: usar estados PENDIENTE, GENERANDO, LISTO y ERROR.
- Regla operativa 00018: mantener ArchivoService solo para validar y guardar archivos.
- Regla operativa 00019: mantener ProcesamientoClient solo para avisar a Django.
- Regla operativa 00020: mantener CallbackController solo para recibir respuesta de Django.
- Regla operativa 00021: en Django usar class Meta con managed = False.
- Regla operativa 00022: no usar max_length en IntegerField.
- Regla operativa 00023: no mostrar tarificacion en tiempo real al usuario final.
- Regla operativa 00024: documentar cambios con commits claros en espanol.
- Regla operativa 00025: mantener Controller -> Service -> Repository -> JPA.
- Regla operativa 00026: no modificar BD ni migraciones sin instruccion explicita.
- Regla operativa 00027: validar permisos de empleado y administrador.
- Regla operativa 00028: no exponer contrasenas ni datos internos innecesarios.
- Regla operativa 00029: usar estados PENDIENTE, GENERANDO, LISTO y ERROR.
- Regla operativa 00030: mantener ArchivoService solo para validar y guardar archivos.
- Regla operativa 00031: mantener ProcesamientoClient solo para avisar a Django.
- Regla operativa 00032: mantener CallbackController solo para recibir respuesta de Django.
- Regla operativa 00033: en Django usar class Meta con managed = False.
- Regla operativa 00034: no usar max_length en IntegerField.
- Regla operativa 00035: no mostrar tarificacion en tiempo real al usuario final.
- Regla operativa 00036: documentar cambios con commits claros en espanol.
- Regla operativa 00037: mantener Controller -> Service -> Repository -> JPA.
- Regla operativa 00038: no modificar BD ni migraciones sin instruccion explicita.
- Regla operativa 00039: validar permisos de empleado y administrador.