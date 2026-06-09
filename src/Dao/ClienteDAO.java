/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import Modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    // GUARDAR
    public boolean guardar(Cliente c) {

        String sql = "INSERT INTO clientes(dni,nombre,telefono,direccion,correo) VALUES(?,?,?,?,?)";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCorreo());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }

    // LISTAR
    public List<Cliente> listar() {

        List<Cliente> lista = new ArrayList<>();

        String sql = "SELECT * FROM clientes";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            rs = ps.executeQuery();

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

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return lista;
    }

    // MODIFICAR
    public boolean modificar(Cliente c) {

        String sql =
        "UPDATE clientes SET "
        + "dni=?, nombre=?, telefono=?, direccion=?, correo=? "
        + "WHERE id=?";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCorreo());
            ps.setInt(6, c.getId());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {

        String sql = "DELETE FROM clientes WHERE id=?";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }
    public Cliente buscarPorDni(String dni) {

    Cliente c = new Cliente();

    String sql = "SELECT * FROM clientes WHERE dni=?";

    try {

        con = Conexion.conectar();

        ps = con.prepareStatement(sql);

        ps.setString(1, dni);

        rs = ps.executeQuery();

        if(rs.next()){

            c.setId(rs.getInt("id"));
            c.setDni(rs.getString("dni"));
            c.setNombre(rs.getString("nombre"));
            c.setTelefono(rs.getString("telefono"));
            c.setDireccion(rs.getString("direccion"));
            c.setCorreo(rs.getString("correo"));
        }

    } catch (Exception e) {

        System.out.println(e.getMessage());
    }

    return c;
}
    public Cliente buscarPorNombre(String nombre) {

    Cliente c = new Cliente();

    String sql = "SELECT * FROM clientes WHERE nombre=?";

    try {

        con = Conexion.conectar();

        ps = con.prepareStatement(sql);

        ps.setString(1, nombre);

        rs = ps.executeQuery();

        if(rs.next()){

            c.setId(rs.getInt("id"));
            c.setNombre(rs.getString("nombre"));
        }

    } catch (Exception e) {

        System.out.println(e.getMessage());
    }

    return c;
}

}
