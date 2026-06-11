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

        jLabel1.setText("DATOS DE LA VENTA");

        jLabel2.setText("Cliente :");

        jLabel3.setText("PRODUCTO");

        jLabel4.setText("Producto :");

        jLabel5.setText("Cantidad :");

        jLabel6.setText("Precio :");

        jLabel7.setText("Subtotal :");

        txtCantidad.addActionListener(this::txtCantidadActionPerformed);

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

        jLabel8.setText("Total :");

        txtTotal.addActionListener(this::txtTotalActionPerformed);

        btnRegistrarVenta.setText("Registrar Venta");
        btnRegistrarVenta.addActionListener(this::btnRegistrarVentaActionPerformed);

        btnAgregar.setText("Agregar Producto");
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);

        btnCancelar.setText("Cancelar Venta");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        cmbCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCliente.addActionListener(this::cmbClienteActionPerformed);

        cmbProducto.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbProducto.addActionListener(this::cmbProductoActionPerformed);

        jLabel9.setText("Descuento");

        txtDescuento.addActionListener(this::txtDescuentoActionPerformed);

        cmbLote.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbLote.addActionListener(this::cmbLoteActionPerformed);

        jLabel10.setText("Tipo de Venta");

        rbProducto.setText("Producto");
        rbProducto.addActionListener(this::rbProductoActionPerformed);

        rbLote.setText("Lote");
        rbLote.addActionListener(this::rbLoteActionPerformed);

        jLabelLote.setText("Seleccionar Lote");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(150, 150, 150))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGap(18, 18, 18))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(28, 28, 28)))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(cmbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(rbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(rbLote, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(cmbCliente, 0, 120, Short.MAX_VALUE)
                                            .addComponent(jLabelLote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(28, 28, 28)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cmbLote, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRegistrarVenta)
                                .addGap(18, 18, 18)
                                .addComponent(btnAgregar)
                                .addGap(18, 18, 18)
                                .addComponent(btnCancelar))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cmbProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbLote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLote))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbProducto)
                    .addComponent(rbLote))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegistrarVenta)
                    .addComponent(btnAgregar)
                    .addComponent(btnCancelar))
                .addGap(18, 18, 18))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnRegistrarVenta;
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
