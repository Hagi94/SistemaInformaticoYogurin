/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import Dao.ClienteDAO;
import Dao.ProductoDAO;
import Dao.ProduccionDAO;
import Modelo.Cliente;
import Modelo.Producto;
import Modelo.Produccion;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import Dao.VentaDAO;
import Modelo.Venta;

public class FrmVentas extends javax.swing.JFrame {
    
    DefaultTableModel modelo = new DefaultTableModel();
    double totalVenta = 0;
    ClienteDAO clienteDAO = new ClienteDAO();
    ProductoDAO productoDAO = new ProductoDAO();
    ProduccionDAO produccionDAO = new ProduccionDAO();
    VentaDAO ventaDAO = new VentaDAO();
    
    private String tipoVenta = "PRODUCTO";

    public FrmVentas() {
        initComponents();
        
        javax.swing.ButtonGroup grupoVenta =
        new javax.swing.ButtonGroup();

        grupoVenta.add(rbProducto);
        grupoVenta.add(rbLote);

        modelo = (DefaultTableModel) tblDetalleVenta.getModel();
        modelo.setRowCount(0);
        
        cargarClientes();
        cargarProductos();
        cargarLotes();
        cargarFechaActual();
        
        txtIdCliente.setEditable(false);
        txtPrecio.setEditable(false);
        txtSubtotal.setEditable(false);
        txtTotal.setEditable(false);
        
        // Ocultar cmbLote inicialmente
        cmbLote.setVisible(false);
        jLabelLote.setVisible(false);
        
        // Seleccionar Producto por defecto
        rbProducto.setSelected(true);
        
        // Listeners para cálculos
        txtCantidad.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularSubtotal();
            }
        });
        
        txtDescuento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularSubtotal();
                actualizarTotal();
            }
        });
    }
    
    private void cargarClientes() {
        cmbCliente.removeAllItems();
        cmbCliente.addItem("-- SELECCIONE --");
        for (Cliente c : clienteDAO.listar()) {
            cmbCliente.addItem(c.getNombre());
        }
    }
    
    private void cargarProductos() {
        cmbProducto.removeAllItems();
        cmbProducto.addItem("-- SELECCIONE --");
        for (Producto p : productoDAO.listar()) {
            if (p.getStock() > 0) {
                cmbProducto.addItem(p.getNombre());
            }
        }
    }
    
    private void cargarLotes() {
        cmbLote.removeAllItems();
        cmbLote.addItem("-- SELECCIONE --");
        for (Produccion prod : produccionDAO.listar()) {
            if (prod.getCantidad() > 0) {
                cmbLote.addItem(prod.getLote() + " - " + prod.getSabor() + " (Stock: " + prod.getCantidad() + ")");
            }
        }
    }
    
    private void cargarFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFecha.setText(sdf.format(new Date()));
        txtFecha.setEditable(false);
    }
    
    private void actualizarTotal() {
        double descuento = 0;
        if (!txtDescuento.getText().isEmpty()) {
            descuento = Double.parseDouble(txtDescuento.getText());
        }
        double totalFinal = totalVenta - descuento;
        txtTotal.setText(String.format("%.2f", totalFinal));
    }
    
    private void calcularSubtotal() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double precio = Double.parseDouble(txtPrecio.getText());
            double subtotal = cantidad * precio;
            txtSubtotal.setText(String.format("%.2f", subtotal));
        } catch (Exception e) {
            txtSubtotal.setText("");
        }
    }
    
    private void limpiarCampos() {
        txtCantidad.setText("");
        txtPrecio.setText("");
        txtSubtotal.setText("");
        if (cmbProducto.getSelectedIndex() > 0) {
            cmbProducto.setSelectedIndex(0);
        }
        if (cmbLote.getSelectedIndex() > 0) {
            cmbLote.setSelectedIndex(0);
        }
    }
      private void agregarProducto() {
        if (cmbProducto.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        
        String productoNombre = cmbProducto.getSelectedItem().toString();
        int cantidad = Integer.parseInt(txtCantidad.getText());
        double precio = Double.parseDouble(txtPrecio.getText());
        
        Producto p = productoDAO.buscarPorNombre(productoNombre);
        
        if (cantidad > p.getStock()) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente. Stock disponible: " + p.getStock());
            return;
        }
        
        double subtotal = cantidad * precio;
        
        Object datos[] = {
            p.getId(),
            productoNombre,
            cantidad,
            precio,
            subtotal,
            "PRODUCTO",
            "-"
        };
        
        modelo.addRow(datos);
        totalVenta += subtotal;
        actualizarTotal();
        
        txtCantidad.setText("");
        txtSubtotal.setText("");
        cmbProducto.setSelectedIndex(0);
    }
    
    private void agregarLote() {
        if (cmbLote.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un lote");
            return;
        }
        
        String seleccion = cmbLote.getSelectedItem().toString();
        String loteNombre = seleccion.split(" - ")[0];
        int cantidad = Integer.parseInt(txtCantidad.getText());
        double precio = Double.parseDouble(txtPrecio.getText());
        
        Produccion prod = produccionDAO.buscarPorLote(loteNombre);
        
        if (cantidad > prod.getCantidad()) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente. Stock disponible: " + prod.getCantidad());
            return;
        }
        
        double subtotal = cantidad * precio;
        
        Object datos[] = {
            prod.getId(),
            prod.getSabor() + " (Lote: " + prod.getLote() + ")",
            cantidad,
            precio,
            subtotal,
            "LOTE",
            prod.getLote()
        };
        
        modelo.addRow(datos);
        totalVenta += subtotal;
        actualizarTotal();
        
        txtCantidad.setText("");
        txtSubtotal.setText("");
        cmbLote.setSelectedIndex(0);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtIdCliente = new javax.swing.JTextField();
        txtCantidad = new javax.swing.JTextField();
        txtPrecio = new javax.swing.JTextField();
        txtSubtotal = new javax.swing.JTextField();
        txtFecha = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDetalleVenta = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        btnRegistrarVenta = new javax.swing.JButton();
        btnAgregar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        cmbCliente = new javax.swing.JComboBox<>();
        cmbProducto = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        txtDescuento = new javax.swing.JTextField();
        cmbLote = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        rbProducto = new javax.swing.JRadioButton();
        rbLote = new javax.swing.JRadioButton();
        jLabelLote = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        lgoVentas = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("DATOS DE LA VENTA");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 9, 123, -1));

        jLabel2.setText("Cliente :");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 46, 60, -1));

        jLabel3.setText("PRODUCTO");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 77, 123, -1));

        jLabel4.setText("Producto :");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 102, 72, -1));

        jLabel5.setText("Cantidad :");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 137, 62, -1));

        jLabel6.setText("Precio :");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 171, 62, -1));

        jLabel7.setText("Subtotal :");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 242, 62, -1));
        getContentPane().add(txtIdCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(84, 43, 141, -1));

        txtCantidad.addActionListener(this::txtCantidadActionPerformed);
        getContentPane().add(txtCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 134, 142, -1));
        getContentPane().add(txtPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 168, 142, -1));
        getContentPane().add(txtSubtotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 236, 142, -1));
        getContentPane().add(txtFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(394, 43, 95, -1));

        tblDetalleVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "PRODUCTO", "CANTIDAD", "PRECIO", "SUBTOTAL", "TIPO", "LOTE"
            }
        ));
        jScrollPane2.setViewportView(tblDetalleVenta);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 36, 700, 303));

        jLabel8.setText("Total :");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 267, 52, -1));

        txtTotal.addActionListener(this::txtTotalActionPerformed);
        getContentPane().add(txtTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 264, 142, -1));

        btnRegistrarVenta.setText("Registrar Venta");
        btnRegistrarVenta.addActionListener(this::btnRegistrarVentaActionPerformed);
        getContentPane().add(btnRegistrarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 304, -1, -1));

        btnAgregar.setText("Agregar Producto");
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        getContentPane().add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 304, -1, -1));

        btnCancelar.setText("Cancelar Venta");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        getContentPane().add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(275, 304, -1, -1));

        cmbCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCliente.addActionListener(this::cmbClienteActionPerformed);
        getContentPane().add(cmbCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(246, 43, 120, -1));

        cmbProducto.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbProducto.addActionListener(this::cmbProductoActionPerformed);
        getContentPane().add(cmbProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(84, 99, 141, -1));

        jLabel9.setText("Descuento");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 211, 72, -1));

        txtDescuento.addActionListener(this::txtDescuentoActionPerformed);
        getContentPane().add(txtDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(84, 208, 95, -1));

        cmbLote.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbLote.addActionListener(this::cmbLoteActionPerformed);
        getContentPane().add(cmbLote, new org.netbeans.lib.awtextra.AbsoluteConstraints(394, 99, 120, -1));

        jLabel10.setText("Tipo de Venta");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(246, 137, 89, -1));

        rbProducto.setText("Producto");
        rbProducto.addActionListener(this::rbProductoActionPerformed);
        getContentPane().add(rbProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(246, 169, 98, -1));

        rbLote.setText("Lote");
        rbLote.addActionListener(this::rbLoteActionPerformed);
        getContentPane().add(rbLote, new org.netbeans.lib.awtextra.AbsoluteConstraints(362, 169, 98, -1));

        jLabelLote.setForeground(new java.awt.Color(0, 0, 5));
        jLabelLote.setText("Seleccionar Lote");
        getContentPane().add(jLabelLote, new org.netbeans.lib.awtextra.AbsoluteConstraints(246, 102, 120, -1));

        btnRegresar.setText("Regresar");
        btnRegresar.addActionListener(this::btnRegresarActionPerformed);
        getContentPane().add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(402, 304, -1, -1));

        lgoVentas.setIcon(new javax.swing.ImageIcon("D:\\ProyectosNetbeans\\SistemaInformatico\\src\\Imagenes\\Botones\\LogoNaranja.png")); // NOI18N
        getContentPane().add(lgoVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1230, 350));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
try {
            if (txtCantidad.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese la cantidad");
                return;
            }
            if (txtPrecio.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto o lote");
                return;
            }
            
            if (tipoVenta.equals("PRODUCTO")) {
                agregarProducto();
            } else {
                agregarLote();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void txtDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescuentoActionPerformed
        actualizarTotal();
    }//GEN-LAST:event_txtDescuentoActionPerformed

    private void btnRegistrarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarVentaActionPerformed
if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Agregue productos a la venta");
            return;
        }
        
        if (txtIdCliente.getText().isEmpty() || cmbCliente.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente");
            return;
        }
        
        Venta venta = new Venta();
        venta.setClienteId(Integer.parseInt(txtIdCliente.getText()));
        venta.setTotal(totalVenta);
        
        double descuento = 0;
        if (!txtDescuento.getText().isEmpty()) {
            descuento = Double.parseDouble(txtDescuento.getText());
        }
        
        venta.setDescuento(descuento);
        venta.setTotalPagar(totalVenta - descuento);
        
        int idVenta = ventaDAO.guardarVenta(venta);
        if(idVenta <= 0){
    JOptionPane.showMessageDialog(this,
            "Error al registrar la venta");
    return;
}
        System.out.println("ID VENTA: " + idVenta);
        for (int i = 0; i < tblDetalleVenta.getRowCount(); i++) {
            
            String tipo = tblDetalleVenta.getValueAt(i, 5).toString();
            int idItem = Integer.parseInt(tblDetalleVenta.getValueAt(i, 0).toString());
            int cantidad = Integer.parseInt(tblDetalleVenta.getValueAt(i, 2).toString());
            double precio = Double.parseDouble(tblDetalleVenta.getValueAt(i, 3).toString());
            double subtotal = Double.parseDouble(tblDetalleVenta.getValueAt(i, 4).toString());
            
            if (tipo.equals("PRODUCTO")) {
                ventaDAO.guardarDetalle(idVenta, idItem, cantidad, precio, subtotal);
                ventaDAO.descontarStock(idItem, cantidad);
                ventaDAO.registrarMovimiento(idItem, cantidad);
            } else {
                String lote = tblDetalleVenta.getValueAt(i, 6).toString();
                ventaDAO.guardarDetalleLote(idVenta, idItem, lote, cantidad, precio, subtotal);
                ventaDAO.descontarStockLote(idItem, cantidad);
            }
  System.out.println(
        tblDetalleVenta.getValueAt(i, 0)
        + " | "
        + tblDetalleVenta.getValueAt(i, 1)
        + " | "
        + tblDetalleVenta.getValueAt(i, 5)
);
        }
        
        JOptionPane.showMessageDialog(null, "✅ Venta registrada correctamente\nTotal: S/ " + String.format("%.2f", totalVenta - descuento));
        
        modelo.setRowCount(0);
        txtCantidad.setText("");
        txtSubtotal.setText("");
        txtTotal.setText("");
        txtDescuento.setText("");
        totalVenta = 0;
        cargarProductos();
        cargarLotes();
    }//GEN-LAST:event_btnRegistrarVentaActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed

        modelo.setRowCount(0);
        txtCantidad.setText("");
        txtSubtotal.setText("");
        txtTotal.setText("");
        txtDescuento.setText("");
        totalVenta = 0;
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void cmbProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProductoActionPerformed
               if (cmbProducto.getSelectedItem() == null || cmbProducto.getSelectedIndex() <= 0) {
            return;
        }
        tipoVenta = "PRODUCTO";
        String nombre = cmbProducto.getSelectedItem().toString();
        Producto p = productoDAO.buscarPorNombre(nombre);
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        calcularSubtotal();
    }//GEN-LAST:event_cmbProductoActionPerformed

    private void txtCantidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadActionPerformed
     calcularSubtotal();
    }//GEN-LAST:event_txtCantidadActionPerformed

    private void rbProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbProductoActionPerformed
        tipoVenta = "PRODUCTO";
        cmbProducto.setVisible(true);
        cmbLote.setVisible(false);
        jLabelLote.setVisible(false);
        jLabel4.setText("Producto :");
        limpiarCampos();
    
    }//GEN-LAST:event_rbProductoActionPerformed

    private void cmbLoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLoteActionPerformed
         if (cmbLote.getSelectedItem() == null || cmbLote.getSelectedIndex() <= 0) {
            return;
        }
        tipoVenta = "LOTE";
        String seleccion = cmbLote.getSelectedItem().toString();
        String lote = seleccion.split(" - ")[0];
        Produccion prod = produccionDAO.buscarPorLote(lote);
if (prod != null) {
    txtPrecio.setText(String.valueOf(prod.getPrecioVenta()));
}
        calcularSubtotal();
    }//GEN-LAST:event_cmbLoteActionPerformed

    private void cmbClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbClienteActionPerformed
if (cmbCliente.getSelectedItem() == null || cmbCliente.getSelectedIndex() <= 0) {
            txtIdCliente.setText("");
            return;
        }
        String nombre = cmbCliente.getSelectedItem().toString();
        Cliente c = clienteDAO.buscarPorNombre(nombre);
        if (c != null) {
            txtIdCliente.setText(String.valueOf(c.getId()));
        }
    }//GEN-LAST:event_cmbClienteActionPerformed

    private void rbLoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbLoteActionPerformed

        tipoVenta = "LOTE";
        cmbProducto.setVisible(false);
        cmbLote.setVisible(true);
        jLabelLote.setVisible(true);
        jLabel4.setText("Lote :");
        cargarLotes();
        limpiarCampos();

    }//GEN-LAST:event_rbLoteActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
     MenuPrincipal menu = new MenuPrincipal();
     menu.setVisible(true);
     
     this.dispose(); // 
// TODO add your handling code here:
    }//GEN-LAST:event_btnRegresarActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnRegistrarVenta;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JComboBox<String> cmbLote;
    private javax.swing.JComboBox<String> cmbProducto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelLote;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lgoVentas;
    private javax.swing.JRadioButton rbLote;
    private javax.swing.JRadioButton rbProducto;
    private javax.swing.JTable tblDetalleVenta;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtDescuento;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIdCliente;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
