/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import util.Conexion;
import Modelo.DetalleVenta;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DetalleVentaDAO {

    Connection con;
    PreparedStatement ps;

    public boolean guardar(DetalleVenta d) {

        String sql =
        "INSERT INTO detalle_venta "
        + "(venta_id,producto_id,cantidad,precio_unitario,subtotal) "
        + "VALUES(?,?,?,?,?)";

        try {

            con = Conexion.getInstancia().getConexion();

            ps = con.prepareStatement(sql);

            ps.setInt(1, d.getVentaId());
            ps.setInt(2, d.getProductoId());
            ps.setInt(3, d.getCantidad());
            ps.setDouble(4, d.getPrecioUnitario());
            ps.setDouble(5, d.getSubtotal());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }
}
