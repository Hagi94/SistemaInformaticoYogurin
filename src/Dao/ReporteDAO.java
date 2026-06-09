package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class ReporteDAO {

    private static final Logger LOGGER = Logger.getLogger(ReporteDAO.class.getName());

    public DefaultTableModel ventasDia() {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Fecha");
        modelo.addColumn("Cliente");
        modelo.addColumn("Total");

        String sql = "SELECT id,fecha,cliente_id,total FROM ventas WHERE DATE(fecha)=CURDATE()";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("fecha"),
                    rs.getInt("cliente_id"),
                    rs.getDouble("total")
                });
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de ventas del día", e);
        }

        return modelo;
    }
}
