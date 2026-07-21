package controlador;

import Dao.UsuarioDAO;
import Modelo.Usuario;
import java.util.List;
import util.Sesion;

/**
 * Controlador de usuarios (RF-01, RF-02, RF-03, RF-23).
 * Valida los datos que llegan de la vista y coordina la interaccion con UsuarioDAO.
 */
public class UsuarioControlador {

    private static final int LARGO_MINIMO_CLAVE = 4;   // (luiggi) regla de negocio, no un numero suelto

    private final UsuarioDAO dao = new UsuarioDAO();

    /** Valida las credenciales e inicia la sesion si son correctas (RF-01). */
    public Resultado iniciarSesion(String usuario, String clave) {

        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            return Resultado.error("Ingrese usuario y contraseña"); // (luiggi) valida antes de consultar la BD
        }

        Usuario u = dao.autenticar(usuario.trim(), clave);

        if (u == null) {
            return Resultado.error("Usuario o contraseña incorrecta");
        }

        Sesion.iniciar(u);                             // (luiggi) deja el usuario disponible para todo el sistema
        return Resultado.exito("Bienvenido " + u.getUsuario() + " (" + u.getRol() + ")");
    }

    /** Cierra la sesion activa (RF-03). */
    public void cerrarSesion() {
        Sesion.cerrar();
    }

    /** Registra un usuario nuevo validando sus datos (RF-02). */
    public Resultado registrar(String usuario, String clave, String rol, boolean estado) {

        String problema = validar(usuario, clave, rol, true);
        if (problema != null) {
            return Resultado.error(problema);
        }

        Usuario u = new Usuario();
        u.setUsuario(usuario.trim());
        u.setClave(clave);
        u.setRol(rol);
        u.setEstado(estado);

        return dao.guardar(u)
                ? Resultado.exito("Usuario registrado correctamente")
                : Resultado.error("No se pudo registrar.\nEs posible que el usuario ya exista."); // (luiggi) explica el motivo probable
    }

    /** Modifica un usuario. La clave vacia significa "conservar la actual". */
    public Resultado modificar(int id, String usuario, String clave, String rol, boolean estado) {

        if (id <= 0) {
            return Resultado.error("Seleccione un usuario de la tabla");
        }

        boolean cambiaClave = clave != null && !clave.isBlank();
        String problema = validar(usuario, clave, rol, cambiaClave);
        if (problema != null) {
            return Resultado.error(problema);
        }

        Usuario u = new Usuario();
        u.setId(id);
        u.setUsuario(usuario.trim());
        u.setClave(cambiaClave ? clave : "");        // (luiggi) vacio = el DAO no toca la clave
        u.setRol(rol);
        u.setEstado(estado);

        return dao.modificar(u)
                ? Resultado.exito("Usuario actualizado")
                : Resultado.error("No se pudo actualizar el usuario");
    }

    /** Elimina un usuario, impidiendo que borre su propia cuenta. */
    public Resultado eliminar(int id) {

        if (id <= 0) {
            return Resultado.error("Seleccione un usuario de la tabla");
        }

        if (Sesion.haySesion() && Sesion.getUsuarioActivo().getId() == id) {
            return Resultado.error("No puede eliminar el usuario con el que inicio sesion"); // (luiggi) evita dejar el sistema sin acceso
        }

        return dao.eliminar(id)
                ? Resultado.exito("Usuario eliminado")
                : Resultado.error("No se pudo eliminar el usuario");
    }

    /** Cambia la contrasena del usuario que tiene la sesion abierta (RF-23). */
    public Resultado cambiarClave(String claveActual, String claveNueva, String claveRepetida) {

        if (!Sesion.haySesion()) {
            return Resultado.error("Debe iniciar sesion");
        }

        if (claveNueva == null || claveNueva.length() < LARGO_MINIMO_CLAVE) {
            return Resultado.error("La nueva clave debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres");
        }

        if (!claveNueva.equals(claveRepetida)) {
            return Resultado.error("La confirmacion no coincide con la nueva clave"); // (luiggi) evita cambiarla por error
        }

        boolean cambiada = dao.cambiarClave(
                Sesion.getUsuarioActivo().getId(), claveActual, claveNueva);

        return cambiada
                ? Resultado.exito("Contraseña actualizada")
                : Resultado.error("La contraseña actual es incorrecta");
    }

    /** Lista los usuarios para mostrarlos en la tabla. */
    public List<Usuario> listar() {
        return dao.listar();
    }

    /** Reglas comunes de validacion de usuario. Devuelve null si todo esta bien. */
    private String validar(String usuario, String clave, String rol, boolean exigeClave) {

        if (usuario == null || usuario.isBlank()) {
            return "El nombre de usuario es obligatorio";
        }
        if (rol == null || rol.isBlank()) {
            return "Seleccione el rol del usuario";
        }
        if (exigeClave && (clave == null || clave.length() < LARGO_MINIMO_CLAVE)) {
            return "La clave debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres";
        }
        return null;                                  // (luiggi) null = datos validos
    }
}
