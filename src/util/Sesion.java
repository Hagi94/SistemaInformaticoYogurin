package util;

import Modelo.Usuario;

/**
 * Clase de utilidad que guarda el usuario autenticado durante la sesion activa.
 * Permite que cualquier ventana consulte quien inicio sesion y con que rol.
 */
public class Sesion {

    private static final String ROL_ADMIN = "Administrador"; // (luiggi) constante, evita numeros/textos magicos

    private static Usuario usuarioActivo;                    // (luiggi) usuario logueado en este momento

    private Sesion() {                                       // (luiggi) clase de utilidad, no se instancia
    }

    /** Guarda el usuario que acaba de iniciar sesion. */
    public static void iniciar(Usuario u) {
        usuarioActivo = u;                                   // (luiggi) registra la sesion tras el login
    }

    /** Devuelve el usuario logueado (null si nadie inicio sesion). */
    public static Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    /** Indica si hay una sesion abierta. */
    public static boolean haySesion() {
        return usuarioActivo != null;                        // (luiggi) protege pantallas contra acceso directo
    }

    /** Indica si el usuario activo tiene rol Administrador. */
    public static boolean esAdministrador() {
        return haySesion() && ROL_ADMIN.equalsIgnoreCase(usuarioActivo.getRol()); // (luiggi) base del control de acceso
    }

    /** Nombre del usuario activo, para mostrarlo en pantalla. */
    public static String getNombreUsuario() {
        return haySesion() ? usuarioActivo.getUsuario() : "";
    }

    /** Limpia la sesion al cerrar sesion. */
    public static void cerrar() {
        usuarioActivo = null;                                // (luiggi) borra el usuario al salir
    }
}
