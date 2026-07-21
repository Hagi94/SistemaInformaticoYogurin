package Dao;

import Modelo.Insumo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.Conexion;

/**
 * DAO de la entidad Insumo (RF-07, RF-08, RF-18).
 * Gestiona el catalogo de materias primas: leche, azucar, cultivo lactico y envases.
 */
public class InsumoDAO {

    /** Registra un insumo nuevo con su unidad de medida y stock minimo (RF-07). */
    public boolean guardar(Insumo i) {

        String sql = "INSERT INTO insumos(nombre,unidad,stock_actual,stock_minimo) VALUES(?,?,?,?)";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidad());
            ps.setDouble(3, i.getStockActual());
            ps.setDouble(4, i.getStockMinimo());              // (luiggi) umbral que dispara la alerta

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar insumo: " + e.getMessage());
            return false;
        }
    }

    /** Lista todos los insumos ordenados por nombre. */
    public List<Insumo> listar() {

        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT id,nombre,unidad,stock_actual,stock_minimo FROM insumos ORDER BY nombre";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    /** Modifica los datos de un insumo existente. */
    public boolean modificar(Insumo i) {

        String sql = "UPDATE insumos SET nombre=?, unidad=?, stock_actual=?, stock_minimo=? WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidad());
            ps.setDouble(3, i.getStockActual());
            ps.setDouble(4, i.getStockMinimo());
            ps.setInt(5, i.getId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al modificar insumo: " + e.getMessage());
            return false;
        }
    }

    /** Elimina un insumo del catalogo. */
    public boolean eliminar(int id) {

        String sql = "DELETE FROM insumos WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al eliminar insumo: " + e.getMessage());
            return false;
        }
    }

    /** Busca un insumo por su id. */
    public Insumo buscarPorId(int id) {

        String sql = "SELECT id,nombre,unidad,stock_actual,stock_minimo FROM insumos WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;         // (luiggi) null si el insumo no existe
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar insumo: " + e.getMessage());
            return null;
        }
    }

    /** Registra la entrada de insumos cuando el administrador realiza una compra (RF-08). */
    public boolean registrarEntrada(int idInsumo, double cantidad) {

        if (cantidad <= 0) {
            System.err.println("La cantidad de la entrada debe ser mayor a cero");
            return false;                                     // (luiggi) valida antes de tocar la BD
        }

        String sql = "UPDATE insumos SET stock_actual = stock_actual + ? WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setDouble(1, cantidad);
            ps.setInt(2, idInsumo);
            return ps.executeUpdate() > 0;                    // (luiggi) suma la compra al stock actual

        } catch (SQLException e) {
            System.err.println("Error al registrar entrada: " + e.getMessage());
            return false;
        }
    }

    /** Devuelve los insumos que llegaron o bajaron de su stock minimo (RF-18). */
    public List<Insumo> listarStockCritico() {

        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT id,nombre,unidad,stock_actual,stock_minimo FROM insumos "
                   + "WHERE stock_actual <= stock_minimo ORDER BY nombre"; // (luiggi) compara contra el minimo real

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar stock critico: " + e.getMessage());
        }
        return lista;
    }

    /** Convierte una fila del ResultSet en un objeto Insumo. */
    private Insumo mapear(ResultSet rs) throws SQLException {
        Insumo i = new Insumo();
        i.setId(rs.getInt("id"));
        i.setNombre(rs.getString("nombre"));
        i.setUnidad(rs.getString("unidad"));
        i.setStockActual(rs.getDouble("stock_actual"));
        i.setStockMinimo(rs.getDouble("stock_minimo"));
        return i;
    }
}
