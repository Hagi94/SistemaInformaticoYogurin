package Dao;

import Modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteDAO {

    private static final Logger LOGGER = Logger.getLogger(ClienteDAO.class.getName());

    // GUARDAR
    public boolean guardar(Cliente c) {
        String sql = "INSERT INTO clientes(dni,nombre,telefono,direccion,correo) VALUES(?,?,?,?,?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCorreo());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar cliente", e);
            return false;
        }
    }

    // LISTAR
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setDni(rs.getString("dni"));
                c.setNombre(rs.getString("nombre"));
                c.setTelefono(rs.getString("telefono"));
                c.setDireccion(rs.getString("direccion"));
                c.setCorreo(rs.getString("correo"));
                lista.add(c);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar clientes", e);
        }

        return lista;
    }

    // MODIFICAR
    public boolean modificar(Cliente c) {
        String sql = "UPDATE clientes SET dni=?, nombre=?, telefono=?, direccion=?, correo=? WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCorreo());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al modificar cliente", e);
            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {
        String sql = "DELETE FROM clientes WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar cliente", e);
            return false;
        }
    }

    public Cliente buscarPorDni(String dni) {
        String sql = "SELECT * FROM clientes WHERE dni=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setDni(rs.getString("dni"));
                    c.setNombre(rs.getString("nombre"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setDireccion(rs.getString("direccion"));
                    c.setCorreo(rs.getString("correo"));
                    return c;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar cliente por DNI", e);
        }

        return new Cliente();
    }

    public Cliente buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM clientes WHERE nombre=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setDni(rs.getString("dni"));
                    c.setNombre(rs.getString("nombre"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setDireccion(rs.getString("direccion"));
                    c.setCorreo(rs.getString("correo"));
                    return c;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar cliente por nombre", e);
        }

        return new Cliente();
    }
}
