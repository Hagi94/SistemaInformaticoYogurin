package controlador;

import java.io.File;
import util.Respaldo;
import util.Sesion;

/**
 * Controlador del respaldo y la restauracion de la base de datos (RF-22).
 * Verifica los permisos antes de ejecutar y traduce el resultado a un mensaje para la vista.
 */
public class RespaldoControlador {

    /** Carpeta donde se guardan las copias. */
    public File carpetaRespaldos() {
        return Respaldo.carpetaRespaldos();
    }

    /** Nombre de archivo sugerido, con fecha y hora. */
    public String nombreSugerido() {
        return Respaldo.nombreSugerido();
    }

    /** Genera una copia de seguridad de la base de datos (RF-22). */
    public Resultado crear(File destino) {

        if (!Sesion.esAdministrador()) {
            return Resultado.error("Solo el administrador puede respaldar la base de datos"); // (luiggi) control de acceso
        }
        if (destino == null) {
            return Resultado.error("Indique donde guardar el respaldo");
        }

        String error = Respaldo.crear(destino);

        return error.isEmpty()
                ? Resultado.exito("Respaldo creado correctamente:\n" + destino.getAbsolutePath()
                        + "\n\nTamano: " + (destino.length() / 1024) + " KB")
                : Resultado.error(error);
    }

    /** Restaura la base de datos desde un archivo previo (RF-22). */
    public Resultado restaurar(File origen) {

        if (!Sesion.esAdministrador()) {
            return Resultado.error("Solo el administrador puede restaurar la base de datos");
        }

        String error = Respaldo.restaurar(origen);

        return error.isEmpty()
                ? Resultado.exito("Base de datos restaurada correctamente")
                : Resultado.error(error);
    }

    /** Texto de advertencia que la vista muestra antes de restaurar. */
    public String textoConfirmacion(File origen) {
        return "Esta accion REEMPLAZA los datos actuales por los del archivo:\n"
             + origen.getName()
             + "\n\nSe perdera todo lo registrado despues de esa copia."
             + "\n\nDesea continuar?";                 // (luiggi) la advertencia vive en el controlador, no en la vista
    }
}
