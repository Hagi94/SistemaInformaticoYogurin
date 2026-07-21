/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import util.Conexion;
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

            con = Conexion.getInstancia().getConexion();

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

            con = Conexion.getInstancia().getConexion();

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

            con = Conexion.getInstancia().getConexion();

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

            con = Conexion.getInstancia().getConexion();

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

        con = Conexion.getInstancia().getConexion();

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

        con = Conexion.getInstancia().getConexion();

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

    /**
     * Historial de compras de un cliente (RF-26).
     * Une clientes, ventas, detalle_venta y productos para mostrar que compro y cuando.
     */
    public javax.swing.table.DefaultTableModel historialCompras(int idCliente) {

        String[] columnas = {"N Venta", "Fecha", "Producto", "Cantidad", "P. Unitario", "Subtotal"};

        javax.swing.table.DefaultTableModel modelo =
                new javax.swing.table.DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;                                   // (luiggi) historial de solo lectura
            }
        };

        String sql = "SELECT v.id, v.fecha, p.nombre, d.cantidad, d.precio_unitario, d.subtotal "
                   + "FROM ventas v "
                   + "INNER JOIN detalle_venta d ON d.venta_id = v.id "   // (luiggi) une la venta con su detalle
                   + "INNER JOIN productos p ON p.id = d.producto_id "    // (luiggi) trae el nombre del producto
                   + "WHERE v.cliente_id = ? "
                   + "ORDER BY v.fecha DESC";

        try (java.sql.PreparedStatement st =
                     Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            st.setInt(1, idCliente);                            // (luiggi) parametrizado, evita inyeccion SQL

            try (java.sql.ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    modelo.addRow(new Object[]{
                        r.getInt("id"), r.getString("fecha"), r.getString("nombre"),
                        r.getInt("cantidad"), r.getDouble("precio_unitario"), r.getDouble("subtotal")
                    });
                }
            }

        } catch (java.sql.SQLException e) {
            System.err.println("Error al cargar historial de compras: " + e.getMessage());
        }
        return modelo;
    }

    /** Cuenta cuantas compras registro un cliente, para mostrarlo en la ficha. */
    public int contarCompras(int idCliente) {

        String sql = "SELECT COUNT(*) AS total FROM ventas WHERE cliente_id = ?";

        try (java.sql.PreparedStatement st =
                     Conexion.getInstancia().getConexion().prepareStatement(sql)) {

            st.setInt(1, idCliente);
            try (java.sql.ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt("total") : 0;         // (luiggi) 0 si el cliente no compro nunca
            }

        } catch (java.sql.SQLException e) {
            System.err.println("Error al contar compras: " + e.getMessage());
            return 0;
        }
    }

}
