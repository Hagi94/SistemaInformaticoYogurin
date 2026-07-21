/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import controlador.ClienteControlador;
import controlador.ProductoControlador;
import controlador.ProduccionControlador;
import Modelo.Cliente;
import Modelo.Producto;
import Modelo.Produccion;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import controlador.VentaControlador;
import Modelo.Venta;

public class FrmVentas extends javax.swing.JFrame {
    
    DefaultTableModel modelo = new DefaultTableModel();
    // La vista solo conversa con controladores, nunca con la capa DAO
    ClienteControlador clienteControl = new ClienteControlador();
    ProductoControlador productoControl = new ProductoControlador();
    ProduccionControlador produccionControl = new ProduccionControlador();
    VentaControlador ventaControl = new VentaControlador();   // (luiggi) mantiene el carrito y la transaccion
    
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
        for (Cliente c : clienteControl.listar()) {
            cmbCliente.addItem(c.getNombre());
        }
    }
    
    private void cargarProductos() {
        cmbProducto.removeAllItems();
        cmbProducto.addItem("-- SELECCIONE --");
        for (Producto p : productoControl.listar()) {
            if (p.getStock() > 0) {
                cmbProducto.addItem(p.getNombre());
            }
        }
    }
    
    private void cargarLotes() {
        cmbLote.removeAllItems();
        cmbLote.addItem("-- SELECCIONE --");
        for (Produccion prod : produccionControl.listar()) {
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
    
    /**
     * Vuelve a pintar la tabla a partir del carrito que mantiene el controlador.
     * La vista ya no lleva su propia copia de las lineas ni del total.
     */
    private void pintarCarrito() {

        modelo.setRowCount(0);                               // (luiggi) limpia antes de repintar

        for (Modelo.ItemVenta item : ventaControl.getCarrito()) {
            modelo.addRow(new Object[]{
                item.getIdItem(),
                item.getDescripcion(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getSubtotal(),
                item.getTipo(),
                item.getLote() == null ? "-" : item.getLote()
            });
        }
        actualizarTotal();
    }

    private void actualizarTotal() {

        controlador.Resultado r = ventaControl.validarDescuento(txtDescuento.getText());

        // Si el descuento aun no es valido se muestra el total sin descontar
        double descuento = r.esExito() ? Double.parseDouble(r.getMensaje()) : 0; // (luiggi) el controlador valida el texto

        txtTotal.setText(String.format("%.2f", ventaControl.calcularTotalPagar(descuento)));
    }
    
    /**
     * Pregunta si se desea imprimir el comprobante de la venta recien registrada (RF-16).
     * Si el usuario acepta, genera el PDF y lo abre.
     */
    private void ofrecerComprobante(int idVenta) {

        int respuesta = JOptionPane.showConfirmDialog(this,
                "Desea generar el comprobante de la venta N " + idVenta + "?",
                "Comprobante", JOptionPane.YES_NO_OPTION);

        if (respuesta != JOptionPane.YES_OPTION) {
            return;                                              // (luiggi) el comprobante es opcional
        }

        controlador.ExportacionControlador exportar = new controlador.ExportacionControlador();

        java.io.File destino = new java.io.File(
                exportar.carpetaDocumentos(), exportar.nombreComprobante(idVenta));

        controlador.Resultado r = exportar.generarComprobante(destino, idVenta);

        JOptionPane.showMessageDialog(this, r.getMensaje(),
                r.esExito() ? "Listo" : "Error",
                r.esExito() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (r.esExito()) {
            abrirArchivo(destino);                               // (luiggi) lo muestra para imprimirlo
        }
    }

    /** Abre el documento con el programa que el sistema tenga asociado. */
    private void abrirArchivo(java.io.File archivo) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            }
        } catch (java.io.IOException e) {
            // No es un fallo de la venta: el archivo ya quedo guardado en disco
            System.err.println("No se pudo abrir el documento: " + e.getMessage());
        }
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
    /** Agrega al carrito el producto elegido. El controlador valida cantidad y stock. */
    private void agregarProducto() {

        if (cmbProducto.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }

        Producto p = productoControl.buscarPorNombre(cmbProducto.getSelectedItem().toString());

        controlador.Resultado r = ventaControl.agregarProducto(p, txtCantidad.getText());

        if (!r.esExito()) {
            JOptionPane.showMessageDialog(this, r.getMensaje());
            return;                                          // (luiggi) el controlador explica que falto
        }

        pintarCarrito();                                     // (luiggi) la tabla se redibuja desde el carrito
        txtCantidad.setText("");
        txtSubtotal.setText("");
        cmbProducto.setSelectedIndex(0);
    }

    /** Agrega al carrito el lote elegido. El controlador valida cantidad y unidades. */
    private void agregarLote() {

        if (cmbLote.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un lote");
            return;
        }

        String loteNombre = cmbLote.getSelectedItem().toString().split(" - ")[0];
        Produccion prod = produccionControl.buscarPorLote(loteNombre);

        controlador.Resultado r = ventaControl.agregarLote(prod, txtCantidad.getText());

        if (!r.esExito()) {
            JOptionPane.showMessageDialog(this, r.getMensaje());
            return;
        }

        pintarCarrito();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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

        lgoVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Botones/LogoNaranja.png"))); // NOI18N
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
        // Toda la validacion la hace el controlador; la vista solo entrega los datos escritos
        controlador.Resultado descuentoValido = ventaControl.validarDescuento(txtDescuento.getText());

        if (!descuentoValido.esExito()) {
            JOptionPane.showMessageDialog(this, descuentoValido.getMensaje());
            return;                                              // (luiggi) descuento mal escrito o mayor al total
        }

        int idCliente = txtIdCliente.getText().isBlank()
                ? 0
                : Integer.parseInt(txtIdCliente.getText());      // (luiggi) 0 = sin cliente elegido

        double descuento = Double.parseDouble(descuentoValido.getMensaje());

        controlador.Resultado r = ventaControl.registrarVenta(idCliente, descuento); // (luiggi) transaccion: todo o nada

        JOptionPane.showMessageDialog(this, r.getMensaje(),
                r.esExito() ? "Listo" : "Atencion",
                r.esExito() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        if (!r.esExito()) {
            return;
        }

        ofrecerComprobante(ventaControl.getUltimoIdVenta());      // (luiggi) comprobante para el cliente (RF-16)

        pintarCarrito();                                         // (luiggi) el carrito quedo vacio tras registrar
        txtCantidad.setText("");
        txtSubtotal.setText("");
        txtTotal.setText("");
        txtDescuento.setText("");
        cargarProductos();
        cargarLotes();
    }//GEN-LAST:event_btnRegistrarVentaActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed

        ventaControl.cancelarVenta();      // (luiggi) vacia el carrito que mantiene el controlador
        pintarCarrito();
        txtCantidad.setText("");
        txtSubtotal.setText("");
        txtDescuento.setText("");
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void cmbProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProductoActionPerformed
               if (cmbProducto.getSelectedItem() == null || cmbProducto.getSelectedIndex() <= 0) {
            return;
        }
        tipoVenta = "PRODUCTO";
        String nombre = cmbProducto.getSelectedItem().toString();
        Producto p = productoControl.buscarPorNombre(nombre);
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
        Produccion prod = produccionControl.buscarPorLote(lote);
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
        Cliente c = clienteControl.buscarPorNombre(nombre);
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
        // El menu principal sigue abierto detras; crear otro lo duplicaba
        this.dispose();   // (luiggi) solo cierra esta ventana y vuelve al menu
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
