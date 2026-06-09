package Dao;

import Modelo.Usuario;
import Utilidades.SegurridadUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {

    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());

    // GUARDAR
    public boolean guardar(Usuario u) {
        String sql = "INSERT INTO usuarios(usuario,clave,salt,rol,estado) VALUES(?,?,?,?,?)";

        String salt = SegurridadUtil.generarSalt();
        String hash = SegurridadUtil.hashearConSalt(u.getClave(), salt);

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getUsuario());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, u.getRol());
            ps.setBoolean(5, u.isEstado());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar usuario", e);
            return false;
        }
    }

    // LISTAR
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id,usuario,clave,rol,estado FROM usuarios";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getBoolean("estado"));
                lista.add(u);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar usuarios", e);
        }

        return lista;
    }

    // MODIFICAR
    public boolean modificar(Usuario u) {
        String sql = "UPDATE usuarios SET usuario=?, clave=?, salt=?, rol=?, estado=? WHERE id=?";

        String hashFinal;
        String saltFinal;
        Credenciales credenciales = obtenerCredencialesPorId(u.getId());

        if (u.getClave() == null || u.getClave().trim().isEmpty()) {
            hashFinal = credenciales.hash;
            saltFinal = credenciales.salt;
        } else if (u.getClave().equals(credenciales.hash)) {
            hashFinal = credenciales.hash;
            saltFinal = credenciales.salt;
        } else {
            saltFinal = SegurridadUtil.generarSalt();
            hashFinal = SegurridadUtil.hashearConSalt(u.getClave(), saltFinal);
        }

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getUsuario());
            ps.setString(2, hashFinal);
            ps.setString(3, saltFinal);
            ps.setString(4, u.getRol());
            ps.setBoolean(5, u.isEstado());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al modificar usuario", e);
            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar usuario", e);
            return false;
        }
    }

    // LOGIN
    public boolean login(String usuario, String clave) {
        String sql = "SELECT clave,salt FROM usuarios WHERE usuario=? AND estado=1";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return SegurridadUtil.verificar(clave, rs.getString("salt"), rs.getString("clave"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al validar login", e);
        }

        return false;
    }

    // OBTENER USUARIO
    public Usuario obtenerUsuario(String usuario, String clave) {
        String sql = "SELECT id,usuario,clave,salt,rol,estado FROM usuarios WHERE usuario=? AND estado=1";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && SegurridadUtil.verificar(clave, rs.getString("salt"), rs.getString("clave"))) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setClave(rs.getString("clave"));
                    u.setRol(rs.getString("rol"));
                    u.setEstado(rs.getBoolean("estado"));
                    return u;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener usuario", e);
        }

        return null;
    }

    public Usuario buscarPorUsuario(String usuario) {
        String sql = "SELECT id,usuario,clave,rol,estado FROM usuarios WHERE usuario=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setClave(rs.getString("clave"));
                    u.setRol(rs.getString("rol"));
                    u.setEstado(rs.getBoolean("estado"));
                    return u;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por nombre", e);
        }

        return null;
    }

    private Credenciales obtenerCredencialesPorId(int id) {
        String sql = "SELECT clave,salt FROM usuarios WHERE id=?";
        Credenciales credenciales = new Credenciales();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    credenciales.hash = rs.getString("clave");
                    credenciales.salt = rs.getString("salt");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener credenciales del usuario", e);
        }

        return credenciales;
    }

    private static class Credenciales {

        private String hash = "";
        private String salt = "";
    }
}
