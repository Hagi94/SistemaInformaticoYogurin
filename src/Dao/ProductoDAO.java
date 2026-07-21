/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import util.Conexion;
import Modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    // GUARDAR
    public boolean guardar(Producto p) {

        String sql = "INSERT INTO productos(nombre,descripcion,precio,stock,estado) VALUES(?,?,?,?,?)";

        try {

            con = Conexion.getInstancia().getConexion();
            ps = con.prepareStatement(sql);

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setBoolean(5, p.isEstado());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error guardar: " + e.getMessage());

            return false;
        }
    }

    // LISTAR
    public List<Producto> listar() {

        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT * FROM productos";

        try {

            con = Conexion.getInstancia().getConexion();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

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

        } catch (Exception e) {

            System.out.println("Error listar: " + e.getMessage());
        }

        return lista;
    }

    // MODIFICAR
    public boolean modificar(Producto p) {

        String sql = "UPDATE productos "
                + "SET nombre=?, descripcion=?, precio=?, stock=? "
                + "WHERE id=?";

        try {

            con = Conexion.getInstancia().getConexion();
            ps = con.prepareStatement(sql);

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getId());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error modificar: " + e.getMessage());

            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {

        String sql = "DELETE FROM productos WHERE id=?";

        try {

            con = Conexion.getInstancia().getConexion();
            ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error eliminar: " + e.getMessage());

            return false;
        }
    }

    // BUSCAR POR ID
    public Producto buscar(int id) {
        
        Producto p = new Producto();

        String sql = "SELECT * FROM productos WHERE id=?";

        try {

            con = Conexion.getInstancia().getConexion();
            ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {

                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getDouble("precio"));
                p.setStock(rs.getInt("stock"));
                p.setEstado(rs.getBoolean("estado"));
            }

        } catch (Exception e) {

            System.out.println("Error buscar: " + e.getMessage());
        }

        return p;
    }
    public Producto buscarPorNombre(String nombre) {

    Producto p = new Producto();

    String sql = "SELECT * FROM productos WHERE nombre=?";

    try {

        con = Conexion.getInstancia().getConexion();

        ps = con.prepareStatement(sql);

        ps.setString(1, nombre);

        rs = ps.executeQuery();

        if(rs.next()){

            p.setId(rs.getInt("id"));
            p.setNombre(rs.getString("nombre"));
            p.setPrecio(rs.getDouble("precio"));
            p.setStock(rs.getInt("stock"));
        }

    } catch (Exception e) {

        System.out.println(e.getMessage());
    }

    return p;
}
}