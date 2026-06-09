/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import Modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    // GUARDAR
    public boolean guardar(Usuario u) {

        String sql =
        "INSERT INTO usuarios(usuario,clave,rol,estado) "
        + "VALUES(?,?,?,?)";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getClave());
            ps.setString(3, u.getRol());
            ps.setBoolean(4, u.isEstado());

            ps.execute();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }

    // LISTAR
    public List<Usuario> listar() {

        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT * FROM usuarios";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            rs = ps.executeQuery();

            while (rs.next()) {

                Usuario u = new Usuario();

                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getBoolean("estado"));

                lista.add(u);
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        return lista;
    }

    // MODIFICAR
    public boolean modificar(Usuario u) {

        String sql =
        "UPDATE usuarios "
        + "SET usuario=?, clave=?, rol=?, estado=? "
        + "WHERE id=?";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getClave());
            ps.setString(3, u.getRol());
            ps.setBoolean(4, u.isEstado());
            ps.setInt(5, u.getId());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }

    // ELIMINAR
    public boolean eliminar(int id) {

        String sql =
        "DELETE FROM usuarios WHERE id=?";

        try {

            con = Conexion.conectar();

            ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ps.execute();

            return true;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return false;
        }
    }
    // LOGIN
public boolean login(String usuario, String clave) {

    boolean acceso = false;

    try {

        con = Conexion.conectar();

        String sql =
                "SELECT * FROM usuarios "
                + "WHERE usuario=? "
                + "AND clave=?";

        ps = con.prepareStatement(sql);

        ps.setString(1, usuario);
        ps.setString(2, clave);

        rs = ps.executeQuery();

        if (rs.next()) {

            acceso = true;

        }

    } catch (Exception e) {

        System.out.println(e.getMessage());

    }

    return acceso;
}
// OBTENER USUARIO
public Usuario obtenerUsuario(
        String usuario,
        String clave) {

    Usuario u = null;

    try {

        con = Conexion.conectar();

        String sql =
                "SELECT * FROM usuarios "
                + "WHERE usuario=? "
                + "AND clave=?";

        ps = con.prepareStatement(sql);

        ps.setString(1, usuario);
        ps.setString(2, clave);

        rs = ps.executeQuery();

        if (rs.next()) {

            u = new Usuario();

            u.setId(
                    rs.getInt("id")
            );

            u.setUsuario(
                    rs.getString("usuario")
            );

            u.setClave(
                    rs.getString("clave")
            );

            u.setRol(
                    rs.getString("rol")
            );

            u.setEstado(
                    rs.getBoolean("estado")
            );
        }

    } catch (Exception e) {

        System.out.println(e.getMessage());
    }

    return u;
}
}