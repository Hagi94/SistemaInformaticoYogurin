package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class ReporteDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    // VENTAS DEL DIA
    public DefaultTableModel ventasDia() {

        DefaultTableModel modelo = new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Fecha");
        modelo.addColumn("Cliente");
        modelo.addColumn("Total");

        try {

            con = Conexion.conectar();

            String sql =
                    "SELECT v.id, v.fecha, c.nombre, v.total_pagar " +
                    "FROM ventas v " +
                    "INNER JOIN clientes c ON v.cliente_id = c.id " +
                    "WHERE DATE(v.fecha)=CURDATE()";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("fecha"),
                    rs.getString("nombre"),
                    rs.getDouble("total_pagar")
                });
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return modelo;
    }

    // VENTAS DEL MES
    public DefaultTableModel ventasMes() {

        DefaultTableModel modelo = new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Fecha");
        modelo.addColumn("Cliente");
        modelo.addColumn("Total");

        try {

            con = Conexion.conectar();

            String sql =
                    "SELECT v.id, v.fecha, c.nombre, v.total_pagar " +
                    "FROM ventas v " +
                    "INNER JOIN clientes c ON v.cliente_id = c.id " +
                    "WHERE MONTH(v.fecha)=MONTH(CURDATE()) " +
                    "AND YEAR(v.fecha)=YEAR(CURDATE())";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("fecha"),
                    rs.getString("nombre"),
                    rs.getDouble("total_pagar")
                });
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return modelo;
    }

    // REPORTE PRODUCCION
    public DefaultTableModel reporteProduccion() {

        DefaultTableModel modelo = new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Fecha");
        modelo.addColumn("Lote");
        modelo.addColumn("Sabor");
        modelo.addColumn("Cantidad");
        modelo.addColumn("Precio Venta");

        try {

            con = Conexion.conectar();

            String sql = "SELECT * FROM produccion";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getDate("fecha"),
                    rs.getString("lote"),
                    rs.getString("sabor"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_venta")
                });
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return modelo;
    }

    // STOCK CRITICO
    public DefaultTableModel stockCritico() {

        DefaultTableModel modelo = new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Producto");
        modelo.addColumn("Stock");
        modelo.addColumn("Precio");

        try {

            con = Conexion.conectar();

            String sql =
                    "SELECT * FROM productos " +
                    "WHERE stock <= 10";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getInt("stock"),
                    rs.getDouble("precio")
                });
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return modelo;
    }

    // CIERRE DE CAJA
    public DefaultTableModel cierreCaja() {

        DefaultTableModel modelo = new DefaultTableModel();

        modelo.addColumn("Fecha");
        modelo.addColumn("Ventas del Día");

        try {

            con = Conexion.conectar();

            String sql =
                    "SELECT CURDATE() AS fecha, " +
                    "IFNULL(SUM(total_pagar),0) AS total " +
                    "FROM ventas " +
                    "WHERE DATE(fecha)=CURDATE()";

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                modelo.addRow(new Object[]{
                    rs.getDate("fecha"),
                    rs.getDouble("total")
                });
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return modelo;
    }
}