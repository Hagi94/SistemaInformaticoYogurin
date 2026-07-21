package Dao;

import Modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.Conexion;
import util.Encriptador;

/**
 * DAO de la entidad Usuario. Encapsula las operaciones JDBC sobre la tabla 'usuarios'.
 * Las contrasenas nunca se guardan ni se comparan en texto plano.
 */
public class UsuarioDAO {

    /** Registra un usuario nuevo cifrando su clave antes de guardarla. */
    public boolean guardar(Usuario u) {

        String sql = "INSERT INTO usuarios(usuario,clave,rol,estado) VALUES(?,?,?,?)";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, u.getUsuario());
            ps.setString(2, Encriptador.encriptar(u.getClave())); // (luiggi) guarda el hash, nunca el texto plano
            ps.setString(3, u.getRol());
            ps.setBoolean(4, u.isEstado());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }

    /** Lista todos los usuarios registrados. */
    public List<Usuario> listar() {

        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id,usuario,clave,rol,estado FROM usuarios ORDER BY id";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {                 // (luiggi) cierra ps y rs automaticamente

            while (rs.next()) {
                lista.add(mapear(rs));                           // (luiggi) arma el objeto desde la fila
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Modifica un usuario. Si la clave viene vacia se conserva la que ya tenia,
     * asi el administrador puede editar rol o estado sin reescribir la contrasena.
     */
    public boolean modificar(Usuario u) {

        boolean cambiaClave = u.getClave() != null && !u.getClave().isBlank(); // (luiggi) decide si toca la clave

        String sql = cambiaClave
                ? "UPDATE usuarios SET usuario=?, clave=?, rol=?, estado=? WHERE id=?"
                : "UPDATE usuarios SET usuario=?, rol=?, estado=? WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, u.getUsuario());
            if (cambiaClave) {
                ps.setString(i++, Encriptador.encriptar(u.getClave())); // (luiggi) recifra solo si la cambio
            }
            ps.setString(i++, u.getRol());
            ps.setBoolean(i++, u.isEstado());
            ps.setInt(i, u.getId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al modificar usuario: " + e.getMessage());
            return false;
        }
    }

    /** Elimina un usuario por su id. */
    public boolean eliminar(int id) {

        String sql = "DELETE FROM usuarios WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida las credenciales del login (RF-01).
     * Busca por nombre de usuario y compara el hash, nunca la clave en texto plano.
     */
    public Usuario autenticar(String usuario, String clavePlana) {

        String sql = "SELECT id,usuario,clave,rol,estado FROM usuarios WHERE usuario=? AND estado=1";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, usuario);                            // (luiggi) parametrizado: evita inyeccion SQL

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;                                 // (luiggi) usuario inexistente o inactivo
                }
                Usuario u = mapear(rs);

                if (!Encriptador.verificar(clavePlana, u.getClave())) { // (luiggi) compara hash contra hash
                    return null;                                 // (luiggi) clave incorrecta
                }

                if (Encriptador.necesitaActualizarse(u.getClave())) {
                    actualizarHash(u.getId(), clavePlana);       // (luiggi) migra la clave al formato nuevo
                }
                return u;                                        // (luiggi) credenciales validas
            }

        } catch (SQLException e) {
            System.err.println("Error al autenticar: " + e.getMessage());
            return null;
        }
    }

    /** Permite al usuario cambiar su propia contrasena (RF-23). */
    public boolean cambiarClave(int idUsuario, String claveActual, String claveNueva) {

        String sqlLeer = "SELECT clave FROM usuarios WHERE id=?";
        String sqlActualizar = "UPDATE usuarios SET clave=? WHERE id=?";

        try {
            Connection con = Conexion.getInstancia().getConexion();

            try (PreparedStatement ps = con.prepareStatement(sqlLeer)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !Encriptador.verificar(claveActual, rs.getString("clave"))) {
                        return false;                            // (luiggi) la clave actual no coincide
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlActualizar)) {
                ps.setString(1, Encriptador.encriptar(claveNueva)); // (luiggi) cifra la clave nueva
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Error al cambiar clave: " + e.getMessage());
            return false;
        }
    }

    /**
     * Regenera el hash de una clave que aun estaba en el formato anterior.
     * Se ejecuta tras un login correcto, de modo que la migracion es transparente:
     * el usuario entra con la misma contrasena de siempre y no se entera del cambio.
     */
    private void actualizarHash(int idUsuario, String clavePlana) {

        String sql = "UPDATE usuarios SET clave=? WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, Encriptador.encriptar(clavePlana)); // (luiggi) vuelve a cifrar con bcrypt
            ps.setInt(2, idUsuario);
            ps.executeUpdate();

        } catch (SQLException e) {
            // No es un fallo del login: el usuario ya fue autenticado correctamente
            System.err.println("No se pudo migrar la clave: " + e.getMessage());
        }
    }

    /** Convierte una fila del ResultSet en un objeto Usuario. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsuario(rs.getString("usuario"));
        u.setClave(rs.getString("clave"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getBoolean("estado"));
        return u;                                                // (luiggi) evita repetir este mapeo en cada metodo
    }
}
