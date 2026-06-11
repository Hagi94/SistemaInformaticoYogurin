/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import Modelo.Produccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProduccionDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    public boolean guardar(Produccion p) {

        String sql = "INSERT INTO produccion "
                + "(fecha, lote, sabor, cantidad, precio_venta, observacion) "
                + "VALUES (?,?,?,?,?,?)";

        try {

            con = Conexion.conectar();
            ps = con.prepareStatement(sql);

            ps.setString(1, p.getFecha());
            ps.setString(2, p.getLote());
            ps.setString(3, p.getSabor());
            ps.setInt(4, p.getCantidad());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setString(6, p.getObservacion());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error al guardar: " + e.getMessage());
            return false;
        }
    }

    public boolean modificar(Produccion p) {

        String sql = "UPDATE produccion "
                + "SET fecha=?, lote=?, sabor=?, cantidad=?, "
                + "precio_venta=?, observacion=? "
                + "WHERE id=?";

        try {

            con = Conexion.conectar();
            ps = con.prepareStatement(sql);

            ps.setString(1, p.getFecha());
            ps.setString(2, p.getLote());
            ps.setString(3, p.getSabor());
            ps.setInt(4, p.getCantidad());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setString(6, p.getObservacion());
            ps.setInt(7, p.getId());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error al modificar: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {

        String sql = "DELETE FROM produccion WHERE id=?";

        try {

            con = Conexion.conectar();
            ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error al eliminar: " + e.getMessage());
            return false;
        }
    }

    public List<Produccion> listar() {

        List<Produccion> lista = new ArrayList<>();

        String sql = "SELECT * FROM produccion ORDER BY id DESC";

        try {

            con = Conexion.conectar();
            ps = con.prepareStatement(sql);

            rs = ps.executeQuery();

            while (rs.next()) {

                Produccion p = new Produccion();

                p.setId(rs.getInt("id"));
                p.setFecha(rs.getString("fecha"));
                p.setLote(rs.getString("lote"));
                p.setSabor(rs.getString("sabor"));
                p.setCantidad(rs.getInt("cantidad"));
                p.setPrecioVenta(rs.getDouble("precio_venta"));
                p.setObservacion(rs.getString("observacion"));

                lista.add(p);
            }

        } catch (Exception e) {

            System.out.println("Error al listar: " + e.getMessage());
        }

        return lista;
    }

    public Produccion buscarPorId(int id) {

        String sql = "SELECT * FROM produccion WHERE id=?";

        try {

            con = Conexion.conectar();
            ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {

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

        } catch (Exception e) {

            System.out.println("Error al buscar: " + e.getMessage());
        }

        return null;
    }

    public Produccion buscarPorLote(String lote) {

        Produccion prod = null;

        String sql = "SELECT * FROM produccion WHERE lote=?";

        try {

            con = Conexion.conectar();
            ps = con.prepareStatement(sql);

            ps.setString(1, lote);

            rs = ps.executeQuery();

            if (rs.next()) {

                prod = new Produccion();

                prod.setId(rs.getInt("id"));
                prod.setFecha(rs.getString("fecha"));
                prod.setLote(rs.getString("lote"));
                prod.setSabor(rs.getString("sabor"));
                prod.setCantidad(rs.getInt("cantidad"));
                prod.setPrecioVenta(rs.getDouble("precio_venta"));
                prod.setObservacion(rs.getString("observacion"));
            }

        } catch (Exception e) {

            System.out.println("Error al buscar lote: " + e.getMessage());
        }

        return prod;
    }
}