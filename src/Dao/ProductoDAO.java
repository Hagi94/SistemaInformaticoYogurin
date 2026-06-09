package Dao;

import Modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductoDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductoDAO.class.getName());

    // GUARDAR
    public boolean guardar(Producto p) {
        String sql = "INSERT INTO productos(nombre,descripcion,precio,stock,estado) VALUES(?,?,?,?,?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setBoolean(5, p.isEstado());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar producto", e);
            return false;
        }
    }

    // LISTAR
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getDouble("precio"));
                p.setStock(rs.getInt("stock"));
                p.setEstado(rs.getBoolean("estado"));
                lista.add(p);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar productos", e);
        }

        return lista;
    }

    // MODIFICAR
    public boolean modificar(Producto p) {
        String sql = "UPDATE productos SET nombre=?, descripcion=?, precio=?, stock=? WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al modificar producto", e);
            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto", e);
            return false;
        }
    }

    // BUSCAR POR ID
    public Producto buscar(int id) {
        String sql = "SELECT * FROM productos WHERE id=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setPrecio(rs.getDouble("precio"));
                    p.setStock(rs.getInt("stock"));
                    p.setEstado(rs.getBoolean("estado"));
                    return p;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar producto por id", e);
        }

        return new Producto();
    }

    public Producto buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM productos WHERE nombre=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecio(rs.getDouble("precio"));
                    p.setStock(rs.getInt("stock"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setEstado(rs.getBoolean("estado"));
                    return p;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar producto por nombre", e);
        }

        return new Producto();
    }
}
