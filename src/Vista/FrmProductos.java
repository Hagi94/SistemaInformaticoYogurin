/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import Dao.ProductoDAO;
import Modelo.Producto;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import javax.swing.JOptionPane;

public class FrmProductos extends javax.swing.JFrame {
    ProductoDAO dao = new ProductoDAO();
    DefaultTableModel modelo = new DefaultTableModel();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmProductos.class.getName());

    /**
     * Creates new form FrmProductos
     */
   public FrmProductos() {
    initComponents();
    btnLimpiar.setText("Limpiar");
    btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

    txtId.setEnabled(false);

    listarProductos();
}

    private void listarProductos() {

    modelo = (DefaultTableModel) tblProductos.getModel();

    modelo.setRowCount(0);

    List<Producto> lista = dao.listar();

    for (Producto p : lista) {

        Object datos[] = {

            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getStock(),
            p.isEstado()

        };

        modelo.addRow(datos);
    }
}
   private void limpiar() {

    txtId.setText("");
    txtNombre.setText("");
    txtDescripcion.setText("");
    txtPrecio.setText("");
    txtStock.setText("");

    txtNombre.requestFocus();
} 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        txtPrecio = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        btnRegresar = new javax.swing.JButton();
        lgoLogoProducto = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("ID :");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 28, 37, -1));
        getContentPane().add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(128, 25, 108, -1));

        jLabel2.setText("Nombre :");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 53, 67, -1));
        getContentPane().add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(128, 53, 252, -1));

        jLabel3.setText("Descripcion :");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 87, 76, -1));
        getContentPane().add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(128, 81, 252, 82));

        jLabel4.setText("Precio :");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 176, 69, -1));

        jLabel5.setText("Stock :");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 210, 76, -1));

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        getContentPane().add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(28, 241, -1, -1));

        btnModificar.setText("Modificar");
        btnModificar.addActionListener(this::btnModificarActionPerformed);
        getContentPane().add(btnModificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(118, 241, -1, -1));

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        getContentPane().add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(217, 241, -1, -1));

        btnLimpiar.setText("Limpiar");
        getContentPane().add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(308, 241, -1, -1));
        getContentPane().add(txtPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(128, 173, 71, -1));
        getContentPane().add(txtStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(128, 207, 71, -1));

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Descripcion", "Precio", "Stock", "Estado"
            }
        ));
        tblProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProductos);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(403, 9, 474, 275));

        btnRegresar.setText("Regresar");
        btnRegresar.addActionListener(this::btnRegresarActionPerformed);
        getContentPane().add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(167, 267, -1, -1));

        lgoLogoProducto.setIcon(new javax.swing.ImageIcon("D:\\ProyectosNetbeans\\SistemaInformatico\\src\\Imagenes\\Botones\\LogoNaranja.png")); // NOI18N
        getContentPane().add(lgoLogoProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1230, 350));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed


    Producto p = new Producto();

    p.setNombre(txtNombre.getText());
    p.setDescripcion(txtDescripcion.getText());
    p.setPrecio(Double.parseDouble(txtPrecio.getText()));
    p.setStock(Integer.parseInt(txtStock.getText()));
    p.setEstado(true);

    if (dao.guardar(p)) {

        JOptionPane.showMessageDialog(
                this,
                "Producto guardado correctamente"
        );

        listarProductos();
        limpiar();

    } else {

        JOptionPane.showMessageDialog(
                this,
                "Error al guardar"
        );
    }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed

    Producto p = new Producto();

    p.setId(Integer.parseInt(txtId.getText()));
    p.setNombre(txtNombre.getText());
    p.setDescripcion(txtDescripcion.getText());
    p.setPrecio(Double.parseDouble(txtPrecio.getText()));
    p.setStock(Integer.parseInt(txtStock.getText()));

    if (dao.modificar(p)) {

        JOptionPane.showMessageDialog(
                this,
                "Producto modificado"
        );

        listarProductos();
        limpiar();
    }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

    int fila = tblProductos.getSelectedRow();

    if (fila == -1) {

        JOptionPane.showMessageDialog(
                this,
                "Seleccione un producto"
        );

        return;
    }

    int id = Integer.parseInt(
            tblProductos.getValueAt(fila, 0).toString()
    );

    dao.eliminar(id);

    listarProductos();

    JOptionPane.showMessageDialog(
            this,
            "Producto eliminado"
    );
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void tblProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductosMouseClicked
   int fila = tblProductos.getSelectedRow();

txtId.setText(tblProductos.getValueAt(fila, 0).toString());
txtNombre.setText(tblProductos.getValueAt(fila, 1).toString());
txtDescripcion.setText(tblProductos.getValueAt(fila, 2).toString());
txtPrecio.setText(tblProductos.getValueAt(fila, 3).toString());
txtStock.setText(tblProductos.getValueAt(fila, 4).toString());
    }//GEN-LAST:event_tblProductosMouseClicked

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
              MenuPrincipal menu  = new MenuPrincipal();
       menu.setVisible(true);
    }//GEN-LAST:event_btnRegresarActionPerformed
private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {

    limpiar();

}
 
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmProductos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lgoLogoProducto;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
