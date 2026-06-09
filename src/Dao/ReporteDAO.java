/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dao;

import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class ReporteDAO {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    public DefaultTableModel ventasDia(){

        DefaultTableModel modelo =
        new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Fecha");
        modelo.addColumn("Cliente");
        modelo.addColumn("Total");

        try{

            con = Conexion.conectar();

            String sql =
            "SELECT * FROM ventas "
            + "WHERE DATE(fecha)=CURDATE()";

            ps = con.prepareStatement(sql);

            rs = ps.executeQuery();

            while(rs.next()){

                modelo.addRow(new Object[]{

                    rs.getInt("id"),
                    rs.getString("fecha"),
                    rs.getInt("cliente_id"),
                    rs.getDouble("total")

                });
            }

        }catch(Exception e){

            System.out.println(e.getMessage());
        }

        return modelo;
    }
}
