package cl.anexocontrol;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DiagnosticoController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/dev/diagnostico")
    public Map<String, Object> diagnostico() {
        Map<String, Object> respuesta = new LinkedHashMap<>();

        try {
            respuesta.put("spring", "OK");
            respuesta.put("oracle", consultarTexto("SELECT 'OK' FROM dual"));
            respuesta.put("usuario_bd", consultarTexto("SELECT USER FROM dual"));

            respuesta.put("tablas", validarTablas());
            respuesta.put("secuencias", validarSecuencias());
            respuesta.put("triggers", validarTriggers());
            respuesta.put("package", validarPackage());
            respuesta.put("conteos", validarConteos());

            respuesta.put("estado_general", "SPRING + ORACLE + OBJETOS BD OK");
        } catch (Exception e) {
            respuesta.put("estado_general", "ERROR");
            respuesta.put("mensaje", e.getMessage());
        }

        return respuesta;
    }

    private Map<String, String> validarTablas() {
        Map<String, String> tablas = new LinkedHashMap<>();

        tablas.put("ROL", existeTabla("ROL"));
        tablas.put("USUARIO", existeTabla("USUARIO"));
        tablas.put("TIPO_REPORTE", existeTabla("TIPO_REPORTE"));
        tablas.put("TIPO_LLAMADA", existeTabla("TIPO_LLAMADA"));
        tablas.put("REGISTRO_LLAMADA", existeTabla("REGISTRO_LLAMADA"));
        tablas.put("REPORTE_TARIFICACION", existeTabla("REPORTE_TARIFICACION"));
        tablas.put("SOLICITUD_REPORTE", existeTabla("SOLICITUD_REPORTE"));
        tablas.put("ERROR_PROCESO", existeTabla("ERROR_PROCESO"));

        return tablas;
    }

    private Map<String, String> validarSecuencias() {
        Map<String, String> secuencias = new LinkedHashMap<>();

        secuencias.put("SEQ_REPORTE_TARIFICACION", existeSecuencia("SEQ_REPORTE_TARIFICACION"));
        secuencias.put("SEQ_SOLICITUD_REPORTE", existeSecuencia("SEQ_SOLICITUD_REPORTE"));
        secuencias.put("SEQ_ERROR_PROCESO", existeSecuencia("SEQ_ERROR_PROCESO"));

        return secuencias;
    }

    private Map<String, String> validarTriggers() {
        Map<String, String> triggers = new LinkedHashMap<>();

        triggers.put("TRG_FECHA_PROCESO_REGISTRO", existeTrigger("TRG_FECHA_PROCESO_REGISTRO"));
        triggers.put("TRG_FECHA_PROCESADO_REPORTE", existeTrigger("TRG_FECHA_PROCESADO_REPORTE"));
        triggers.put("TRG_ESTADO_SOLICITUD_DEFAULT", existeTrigger("TRG_ESTADO_SOLICITUD_DEFAULT"));
        triggers.put("TRG_VALIDAR_DURACION", existeTrigger("TRG_VALIDAR_DURACION"));

        return triggers;
    }

    private Map<String, String> validarPackage() {
        Map<String, String> packages = new LinkedHashMap<>();

        packages.put("PKG_TARIFICACION", existeObjeto("PKG_TARIFICACION", "PACKAGE"));
        packages.put("PKG_TARIFICACION_BODY", existeObjeto("PKG_TARIFICACION", "PACKAGE BODY"));

        return packages;
    }

    private Map<String, Integer> validarConteos() {
        Map<String, Integer> conteos = new LinkedHashMap<>();

        conteos.put("ROL", contar("ROL"));
        conteos.put("USUARIO", contar("USUARIO"));
        conteos.put("TIPO_REPORTE", contar("TIPO_REPORTE"));
        conteos.put("TIPO_LLAMADA", contar("TIPO_LLAMADA"));
        conteos.put("REGISTRO_LLAMADA", contar("REGISTRO_LLAMADA"));
        conteos.put("REPORTE_TARIFICACION", contar("REPORTE_TARIFICACION"));
        conteos.put("SOLICITUD_REPORTE", contar("SOLICITUD_REPORTE"));
        conteos.put("ERROR_PROCESO", contar("ERROR_PROCESO"));

        return conteos;
    }

    private String existeTabla(String nombreTabla) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_tables WHERE table_name = ?",
                Integer.class,
                nombreTabla
        );

        return total != null && total > 0 ? "OK" : "NO ENCONTRADA";
    }

    private String existeSecuencia(String nombreSecuencia) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_sequences WHERE sequence_name = ?",
                Integer.class,
                nombreSecuencia
        );

        return total != null && total > 0 ? "OK" : "NO ENCONTRADA";
    }

    private String existeTrigger(String nombreTrigger) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_triggers WHERE trigger_name = ?",
                Integer.class,
                nombreTrigger
        );

        return total != null && total > 0 ? "OK" : "NO ENCONTRADO";
    }

    private String existeObjeto(String nombreObjeto, String tipoObjeto) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_objects WHERE object_name = ? AND object_type = ?",
                Integer.class,
                nombreObjeto,
                tipoObjeto
        );

        return total != null && total > 0 ? "OK" : "NO ENCONTRADO";
    }

    private Integer contar(String tabla) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
    }

    private String consultarTexto(String sql) {
        return jdbcTemplate.queryForObject(sql, String.class);
    }
}