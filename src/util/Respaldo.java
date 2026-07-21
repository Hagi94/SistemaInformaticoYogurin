package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Clase de utilidad para el respaldo y la restauracion de la base de datos (RF-22).
 * Invoca las herramientas mysqldump.exe y mysql.exe que ya vienen incluidas con XAMPP,
 * por lo que no requiere ninguna libreria externa.
 */
public class Respaldo {

    /** Carpeta donde se guardan las copias, dentro del proyecto. */
    public static final String CARPETA = "respaldos";

    private static final int TIMEOUT_MINUTOS = 5;   // (luiggi) evita que el sistema se congele si algo falla

    /** Rutas habituales de instalacion, se prueban en orden. */
    private static final String[] RUTAS_POSIBLES = {
        "C:/xampp/mysql/bin/",
        "C:/wamp64/bin/mysql/mysql8.0.31/bin/",
        "C:/laragon/bin/mysql/mysql-8.0.30-winx64/bin/",
        "C:/Program Files/MySQL/MySQL Server 8.0/bin/",
        ""                                          // (luiggi) ultimo intento: que este en el PATH
    };

    private Respaldo() {                            // (luiggi) clase de utilidad, no se instancia
    }

    /** Nombre sugerido para el archivo, con fecha y hora para no sobrescribir copias. */
    public static String nombreSugerido() {
        String marca = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm")); // (luiggi) evita nombres repetidos
        return ConfigBD.NOMBRE_BD + "_" + marca + ".sql";
    }

    /** Carpeta de respaldos; la crea si todavia no existe. */
    public static File carpetaRespaldos() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();                       // (luiggi) crea la carpeta la primera vez
        }
        return carpeta;
    }

    /**
     * Genera una copia completa de la base de datos en el archivo indicado (RF-22).
     *
     * @return mensaje vacio si todo salio bien, o la descripcion del error.
     */
    public static String crear(File destino) {

        File herramienta = localizar("mysqldump.exe");
        if (herramienta == null) {
            return "No se encontro mysqldump.exe.\nVerifique que XAMPP este instalado en C:\\xampp";
        }

        List<String> comando = new ArrayList<>();
        comando.add(herramienta.getAbsolutePath());
        comando.add("--host=" + ConfigBD.HOST);
        comando.add("--port=" + ConfigBD.PUERTO);
        comando.add("--user=" + ConfigBD.USUARIO);
        comando.add("--default-character-set=utf8mb4"); // (luiggi) conserva tildes y la letra n con virgulilla
        comando.add("--routines");
        comando.add("--events");
        comando.add(ConfigBD.NOMBRE_BD);

        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            aplicarClave(pb);                        // (luiggi) la clave viaja por variable de entorno, no por consola
            pb.redirectOutput(destino);              // (luiggi) la salida del volcado va directo al archivo

            File errores = File.createTempFile("respaldo", ".log");
            pb.redirectError(errores);

            Process proceso = pb.start();

            if (!proceso.waitFor(TIMEOUT_MINUTOS, TimeUnit.MINUTES)) {
                proceso.destroyForcibly();           // (luiggi) corta el proceso si se quedo colgado
                return "El respaldo tardo demasiado y fue cancelado";
            }

            String detalle = leerYBorrar(errores);

            if (proceso.exitValue() != 0) {
                return "mysqldump termino con error:\n" + detalle;
            }

            if (destino.length() == 0) {
                return "El archivo se genero vacio:\n" + detalle; // (luiggi) detecta respaldos inservibles
            }

            return "";                               // (luiggi) cadena vacia = exito

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();   // (luiggi) buena practica al capturar InterruptedException
            }
            return "Error al ejecutar el respaldo: " + e.getMessage();
        }
    }

    /**
     * Restaura la base de datos desde un archivo .sql generado previamente.
     * ATENCION: reemplaza los datos actuales, quien la invoque debe pedir confirmacion.
     *
     * @return mensaje vacio si todo salio bien, o la descripcion del error.
     */
    public static String restaurar(File origen) {

        if (origen == null || !origen.isFile()) {
            return "El archivo de respaldo no existe";
        }

        File herramienta = localizar("mysql.exe");
        if (herramienta == null) {
            return "No se encontro mysql.exe.\nVerifique que XAMPP este instalado en C:\\xampp";
        }

        List<String> comando = new ArrayList<>();
        comando.add(herramienta.getAbsolutePath());
        comando.add("--host=" + ConfigBD.HOST);
        comando.add("--port=" + ConfigBD.PUERTO);
        comando.add("--user=" + ConfigBD.USUARIO);
        comando.add("--default-character-set=utf8mb4");
        comando.add(ConfigBD.NOMBRE_BD);

        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            aplicarClave(pb);
            pb.redirectInput(origen);                // (luiggi) el archivo .sql entra como entrada del comando

            File errores = File.createTempFile("restauracion", ".log");
            pb.redirectError(errores);

            Process proceso = pb.start();

            if (!proceso.waitFor(TIMEOUT_MINUTOS, TimeUnit.MINUTES)) {
                proceso.destroyForcibly();
                return "La restauracion tardo demasiado y fue cancelada";
            }

            String detalle = leerYBorrar(errores);

            if (proceso.exitValue() != 0) {
                return "La restauracion fallo:\n" + detalle;
            }

            Conexion.getInstancia().cerrar();        // (luiggi) fuerza reconectar para leer los datos restaurados
            return "";

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "Error al ejecutar la restauracion: " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------
    // Metodos privados de apoyo
    // ------------------------------------------------------------------

    /** Busca el ejecutable en las rutas habituales de instalacion. */
    private static File localizar(String ejecutable) {
        for (String ruta : RUTAS_POSIBLES) {
            File f = new File(ruta + ejecutable);
            if (f.isFile()) {
                return f;                            // (luiggi) devuelve la primera ruta valida encontrada
            }
        }
        return null;
    }

    /** Pasa la clave por variable de entorno para que no quede visible en la linea de comandos. */
    private static void aplicarClave(ProcessBuilder pb) {
        if (ConfigBD.CLAVE != null && !ConfigBD.CLAVE.isEmpty()) {
            pb.environment().put("MYSQL_PWD", ConfigBD.CLAVE); // (luiggi) mas seguro que usar -p en el comando
        }
    }

    /** Lee el archivo temporal de errores y luego lo elimina. */
    private static String leerYBorrar(File log) {
        try {
            String texto = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8).trim();
            Files.deleteIfExists(log.toPath());      // (luiggi) no deja archivos temporales sueltos
            return texto.isEmpty() ? "(sin detalle)" : texto;
        } catch (IOException e) {
            return "(no se pudo leer el detalle del error)";
        }
    }
}
