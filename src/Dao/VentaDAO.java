/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import Modelo.Venta;
import java.sql.*;

public class VentaDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    public int guardarVenta(Venta v) {

        int idVenta = 0;

        String sql =
        "INSERT INTO ventas(fecha,cliente_id,total,descuento,total_pagar) "
        + "VALUES(NOW(),?,?,?,?)";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setInt(1, v.getClienteId());
            ps.setDouble(2, v.getTotal());
            ps.setDouble(3, v.getDescuento());
            ps.setDouble(4, v.getTotalPagar());

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();

            if(rs.next()){

                idVenta = rs.getInt(1);
            }

        } catch(Exception e){
         e.printStackTrace();
         
}
        

        return idVenta;
    }
    public void guardarDetalle(
        int ventaId,
        int productoId,
        int cantidad,
        double precio,
        double subtotal) {

    String sql =
    "INSERT INTO detalle_venta "
    + "(venta_id,producto_id,cantidad,"
    + "precio_unitario,subtotal) "
    + "VALUES(?,?,?,?,?)";

    try {

        con = Conexion.conectar();

        ps = con.prepareStatement(sql);

        ps.setInt(1, ventaId);
        ps.setInt(2, productoId);
        ps.setInt(3, cantidad);
        ps.setDouble(4, precio);
        ps.setDouble(5, subtotal);

        ps.executeUpdate();

    } catch(Exception e){

        System.out.println(e.getMessage());
    }
}
    public void descontarStock(
        int productoId,
        int cantidad) {

    String sql =
    "UPDATE productos "
    + "SET stock = stock - ? "
    + "WHERE id=?";

    try {

        con = Conexion.conectar();

        ps = con.prepareStatement(sql);

        ps.setInt(1, cantidad);
        ps.setInt(2, productoId);

        ps.executeUpdate();

    } catch(Exception e){

        System.out.println(e.getMessage());
    }
}
    public void registrarMovimiento(
        int productoId,
        int cantidad) {

    String sql =
    "INSERT INTO movimientos_inventario"
    + "(tipo,producto_id,cantidad,observacion)"
    + " VALUES('SALIDA',?,?,?)";

    try {

        con = Conexion.conectar();

        ps = con.prepareStatement(sql);

        ps.setInt(1, productoId);
        ps.setInt(2, cantidad);
        ps.setString(3, "Venta realizada");

        ps.executeUpdate();

    } catch(Exception e){

        System.out.println(e.getMessage());
    }
}
  public void guardarDetalleLote(
        int ventaId,
        int produccionId,
        String lote,
        int cantidad,
        double precio,
        double subtotal) {

    String sql =
        "INSERT INTO detalle_venta_lote " +
        "(venta_id,produccion_id,lote,cantidad,precio_unitario,subtotal) " +
        "VALUES(?,?,?,?,?,?)";

    try {

        con = Conexion.conectar();
        ps = con.prepareStatement(sql);

        ps.setInt(1, ventaId);
        ps.setInt(2, produccionId);
        ps.setString(3, lote);
        ps.setInt(4, cantidad);
        ps.setDouble(5, precio);
        ps.setDouble(6, subtotal);

        ps.executeUpdate();

    } catch (Exception e) {

        System.out.println(e.getMessage());
    }
}

public void descontarStockLote(
        int produccionId,
        int cantidad) {

    String sql =
        "UPDATE produccion " +
        "SET cantidad = cantidad - ? " +
        "WHERE id = ?";

    try {

        con = Conexion.conectar();
        ps = con.prepareStatement(sql);

        ps.setInt(1, cantidad);
        ps.setInt(2, produccionId);

        ps.executeUpdate();

    } catch (Exception e) {

        System.out.println(e.getMessage());
    }
}  
}
