package cl.anexocontrol.Archivo.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ArchivoService {

    // ruta donde se guardaran los archivos temporalmente
    private static final String RUTA_BASE = "archivos/pendientes";

    // guarda el archivo subido y devuelve la ruta donde quedo
    public String guardarArchivo(MultipartFile archivo, Long idCarga) {
        // comprobamos que el archivo venga con algo
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("El archivo es obligatorio");
        }

        // sacamos el nombre del archivo
        String nombreOriginal = archivo.getOriginalFilename();

        // validamos que el nombre no este vacio
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new RuntimeException("El nombre del archivo no es valido");
        }

        // pasamos el nombre a minusculas para revisar la extension mas facil
        String nombreLower = nombreOriginal.toLowerCase();

        // solo aceptamos csv o txt
        if (!nombreLower.endsWith(".csv") && !nombreLower.endsWith(".txt")) {
            throw new RuntimeException("Solo se permiten archivos CSV o TXT");
        }

        try {
            // preparamos la ruta y creamos la carpeta si no existe (usamos ruta absoluta para evitar errores de Spring)
            Path carpetaDestino = Paths.get(RUTA_BASE).toAbsolutePath().normalize();
            Files.createDirectories(carpetaDestino);

            // armamos un nombre unico juntando la carga y el nombre original
            String nombreArchivo = "carga_" + idCarga + "_" + nombreOriginal;
            Path rutaArchivo = carpetaDestino.resolve(nombreArchivo);

            // copiamos el archivo a su nueva casa
            archivo.transferTo(rutaArchivo.toFile());

            // devolvemos la ruta final como texto
            return rutaArchivo.toString();

        } catch (IOException e) {
            // imprimimos el error real en la consola por si acaso
            e.printStackTrace();
            // si algo explota tiramos error general
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }
}
