package Dao;

import Modelo.Produccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import util.Conexion;

/**
 * DAO de la entidad Produccion (RF-09, RF-10).
 * El registro de un lote descuenta los insumos consumidos dentro de una transaccion JDBC.
 */
public class ProduccionDAO {

    private static final String SQL_LOTE =
            "INSERT INTO produccion(fecha,lote,sabor,cantidad,precio_venta,observacion) VALUES(?,?,?,?,?,?)";

    // Descuenta el insumo solo si hay existencia suficiente
    private static final String SQL_DESCONTAR_INSUMO =
            "UPDATE insumos SET stock_actual = stock_actual - ? WHERE id=? AND stock_actual >= ?";

    private static final String SQL_INSUMO_USADO =
            "INSERT INTO produccion_insumo(produccion_id,insumo_id,cantidad_usada) VALUES(?,?,?)";

    /**
     * Registra un lote de produccion y descuenta los insumos utilizados (RF-09, RF-10).
     * Si algun insumo no alcanza, se revierte todo y el lote no se guarda.
     *
     * @param insumosUsados pares idInsumo -> cantidad consumida. Puede ir vacio.
     * @return el id del lote creado, o 0 si la operacion fue revertida.
     */
    public int registrarLote(Produccion p, Map<Integer, Double> insumosUsados) {

        Connection con = Conexion.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);                        // (luiggi) inicia la transaccion

            int idLote = insertarLote(con, p);               // (luiggi) guarda el lote y obtiene su id

            if (insumosUsados != null) {
                for (Map.Entry<Integer, Double> uso : insumosUsados.entrySet()) {
                    descontarInsumo(con, idLote, uso.getKey(), uso.getValue()); // (luiggi) resta cada insumo
                }
            }

            con.commit();                                    // (luiggi) confirma lote e insumos juntos
            return idLote;

        } catch (SQLException e) {
            revertir(con);                                   // (luiggi) deshace todo si falto algun insumo
            System.err.println("Lote revertido: " + e.getMessage());
            return 0;

        } finally {
            restaurarAutoCommit(con);
        }
    }

    /** Registro simple de un lote, sin consumo de insumos. */
    public boolean guardar(Produccion p) {
        return registrarLote(p, null) > 0;                   // (luiggi) reutiliza la version transaccional
    }

    /** Modifica los datos de un lote existente. */
    public boolean modificar(Produccion p) {

        String sql = "UPDATE produccion SET fecha=?, lote=?, sabor=?, cantidad=?, "
                   + "precio_venta=?, observacion=? WHERE id=?";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, p.getFecha());
            ps.setString(2, p.getLote());
            ps.setString(3, p.getSabor());
            ps.setInt(4, p.getCantidad());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setString(6, p.getObservacion());
            ps.setInt(7, p.getId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al modificar lote: " + e.getMessage());
            return false;
        }
    }

    /** Elimina un lote y el registro de los insumos que consumio. */
    public boolean eliminar(int id) {

        Connection con = Conexion.getInstancia().getConexion();

        try {
            con.setAutoCommit(false);                        // (luiggi) borra lote y detalle como una sola unidad

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM produccion_insumo WHERE produccion_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();                          // (luiggi) primero el hijo por la clave foranea
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM produccion WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            revertir(con);
            System.err.println("Error al eliminar lote: " + e.getMessage());
            return false;

        } finally {
            restaurarAutoCommit(con);
        }
    }

    /** Lista todos los lotes de produccion registrados. */
    public List<Produccion> listar() {

        List<Produccion> lista = new ArrayList<>();
        String sql = "SELECT id,fecha,lote,sabor,cantidad,precio_venta,observacion "
                   + "FROM produccion ORDER BY fecha DESC";

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar lotes: " + e.getMessage());
        }
        return lista;
    }

    /** Busca un lote por su id. */
    public Produccion buscarPorId(int id) {
        return buscarPor("id", String.valueOf(id));
    }

    /** Busca un lote por su codigo. */
    public Produccion buscarPorLote(String lote) {
        return buscarPor("lote", lote);
    }

    // ------------------------------------------------------------------
    // Metodos privados de apoyo
    // ------------------------------------------------------------------

    /** Inserta el lote en 'produccion' y devuelve el id autogenerado. */
    private int insertarLote(Connection con, Produccion p) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SQL_LOTE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getFecha());
            ps.setString(2, p.getLote());
            ps.setString(3, p.getSabor());
            ps.setInt(4, p.getCantidad());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setString(6, p.getObservacion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("MySQL no devolvio el id del lote");
                }
                return rs.getInt(1);
            }
        }
    }

    /** Resta un insumo del almacen y deja constancia de cuanto consumio el lote. */
    private void descontarInsumo(Connection con, int idLote, int idInsumo, double cantidad)
            throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SQL_DESCONTAR_INSUMO)) {
            ps.setDouble(1, cantidad);
            ps.setInt(2, idInsumo);
            ps.setDouble(3, cantidad);

            if (ps.executeUpdate() == 0) {                   // (luiggi) 0 filas = insumo insuficiente
                throw new SQLException("Stock insuficiente del insumo id " + idInsumo);
            }
        }

        try (PreparedStatement ps = con.prepareStatement(SQL_INSUMO_USADO)) {
            ps.setInt(1, idLote);
            ps.setInt(2, idInsumo);
            ps.setDouble(3, cantidad);
            ps.executeUpdate();                              // (luiggi) tabla intermedia de la relacion N:M
        }
    }

    /** Busca un lote por la columna indicada (id o lote). */
    private Produccion buscarPor(String columna, String valor) {

        String sql = "SELECT id,fecha,lote,sabor,cantidad,precio_venta,observacion "
                   + "FROM produccion WHERE " + columna + "=?"; // (luiggi) columna fija en codigo, valor parametrizado

        try (PreparedStatement ps = Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar lote: " + e.getMessage());
            return null;
        }
    }

    /** Convierte una fila del ResultSet en un objeto Produccion. */
    private Produccion mapear(ResultSet rs) throws SQLException {
        Produccion p = new Produccion();
        p.setId(rs.getInt("id"));
        p.setFecha(rs.getString("fecha"));
        p.setLote(rs.getString("lote"));
        p.setSabor(rs.getString("sabor"));
        p.setCantidad(rs.getInt("cantidad"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setObservacion(rs.getString("observacion"));
        return p;
    }

    /** Deshace la transaccion cuando ocurre un error. */
    private void revertir(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ex) {
            System.err.println("Error en el rollback: " + ex.getMessage());
        }
    }

    /** Devuelve la conexion al modo autocommit. */
    private void restaurarAutoCommit(Connection con) {
        try {
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            System.err.println("Error al restaurar autocommit: " + ex.getMessage());
        }
    }
}
