/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

/**
 *
 * @author hagi1
 */
public class FrmInsumos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmInsumos.class.getName());

    /**
     * Creates new form FrmInsumos
     */
    private final controlador.InsumoControlador control = new controlador.InsumoControlador(); // (luiggi) la vista solo habla con el controlador
    private int idSeleccionado = 0;                          // (luiggi) 0 = no hay insumo elegido

    public FrmInsumos() {
        initComponents();
        prepararPantalla();                                  // (luiggi) conecta la interfaz con el controlador
        listarInsumos();
        listarAlertas();
    }

    /** Configura tablas, botones y menu sin modificar el codigo generado por NetBeans. */
    private void prepararPantalla() {

        setTitle("Gestion de Insumos");
        setLocationRelativeTo(null);                         // (luiggi) centra la ventana
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); // (luiggi) cerrar no apaga el sistema

        limpiarCampos();

        btnGuardar.addActionListener(e -> guardarInsumo());   // (luiggi) alta de insumo (RF-07)
        btnModificar.addActionListener(e -> modificarInsumo());

        // Al hacer clic en una fila se cargan sus datos en el formulario
        jTable2.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();                            // (luiggi) pasa la fila elegida a los campos
            }
        });

        construirMenu();
    }

    /** Menu superior con la entrada de compras y el refresco de alertas. */
    private void construirMenu() {

        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu menu = new javax.swing.JMenu("Operaciones");

        javax.swing.JMenuItem itemEntrada = new javax.swing.JMenuItem("Registrar entrada de compra");
        itemEntrada.addActionListener(e -> registrarEntrada());  // (luiggi) suma stock por compra (RF-08)

        javax.swing.JMenuItem itemRefrescar = new javax.swing.JMenuItem("Actualizar listas");
        itemRefrescar.addActionListener(e -> { listarInsumos(); listarAlertas(); });

        menu.add(itemEntrada);
        menu.add(itemRefrescar);
        barra.add(menu);
        setJMenuBar(barra);                                   // (luiggi) el menu no altera el layout del formulario
    }

    // ------------------------------------------------------------------
    // Listados
    // ------------------------------------------------------------------

    /** Carga todos los insumos en la tabla principal. */
    private void listarInsumos() {

        String[] columnas = {"ID", "Nombre", "Unidad", "Stock Actual", "Stock Minimo"};
        javax.swing.table.DefaultTableModel modelo =
                new javax.swing.table.DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;                                 // (luiggi) el stock solo cambia por operaciones
            }
        };

        for (Modelo.Insumo i : control.listar()) {
            modelo.addRow(new Object[]{
                i.getId(), i.getNombre(), i.getUnidad(), i.getStockActual(), i.getStockMinimo()
            });
        }
        jTable2.setModel(modelo);
    }

    /** Carga en la tabla superior los insumos que llegaron al stock minimo (RF-18). */
    private void listarAlertas() {

        String[] columnas = {"ALERTA - Insumo", "Unidad", "Actual", "Minimo"};
        javax.swing.table.DefaultTableModel modelo =
                new javax.swing.table.DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };

        java.util.List<Modelo.Insumo> criticos = control.listarStockCritico();

        for (Modelo.Insumo i : criticos) {
            modelo.addRow(new Object[]{
                i.getNombre(), i.getUnidad(), i.getStockActual(), i.getStockMinimo()
            });
        }
        jTable1.setModel(modelo);

        jTable1.setBackground(criticos.isEmpty()               // (luiggi) rojo suave solo si hay faltantes
                ? java.awt.Color.WHITE
                : new java.awt.Color(255, 228, 228));
    }

    // ------------------------------------------------------------------
    // Operaciones
    // ------------------------------------------------------------------

    /** Registra un insumo nuevo (RF-07). */
    private void guardarInsumo() {

        // La vista entrega el texto tal cual; el controlador valida y decide
        mostrar(control.registrar(                            // (luiggi) delega la validacion al controlador
                txtNombre.getText(), txtUnidad.getText(),
                txtStockActual.getText(), txtStockMinimo.getText()));
    }

    /** Modifica el insumo seleccionado en la tabla. */
    private void modificarInsumo() {

        mostrar(control.modificar(idSeleccionado,
                txtNombre.getText(), txtUnidad.getText(),
                txtStockActual.getText(), txtStockMinimo.getText()));
    }

    /** Suma al stock la cantidad comprada de un insumo (RF-08). */
    private void registrarEntrada() {

        if (idSeleccionado == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione primero el insumo comprado");
            return;                                           // (luiggi) sin seleccion no tiene que preguntar nada
        }

        String texto = javax.swing.JOptionPane.showInputDialog(this,
                "Cantidad comprada de " + txtNombre.getText() + " (" + txtUnidad.getText() + "):");

        if (texto == null) {
            return;                                           // (luiggi) el usuario cancelo el dialogo
        }

        mostrar(control.registrarEntrada(idSeleccionado, texto));
    }

    /**
     * Muestra el mensaje del controlador y refresca las listas si la operacion salio bien.
     * Concentra aqui lo que antes se repetia en cada metodo.
     */
    private void mostrar(controlador.Resultado r) {

        javax.swing.JOptionPane.showMessageDialog(this, r.getMensaje(),
                r.esExito() ? "Listo" : "Atencion",
                r.esExito() ? javax.swing.JOptionPane.INFORMATION_MESSAGE
                            : javax.swing.JOptionPane.WARNING_MESSAGE); // (luiggi) el icono depende del resultado

        if (r.esExito()) {
            limpiarCampos();
            listarInsumos();
            listarAlertas();                                  // (luiggi) refresca alertas tras cada cambio
        }
    }

    // ------------------------------------------------------------------
    // Formulario
    // ------------------------------------------------------------------

    // La validacion de los campos ya no vive aqui: la hace InsumoControlador

    /** Pasa la fila seleccionada de la tabla al formulario. */
    private void cargarSeleccion() {

        int fila = jTable2.getSelectedRow();
        if (fila < 0) {
            return;
        }

        idSeleccionado = Integer.parseInt(jTable2.getValueAt(fila, 0).toString()); // (luiggi) recuerda que insumo se edita
        txtNombre.setText(jTable2.getValueAt(fila, 1).toString());
        txtUnidad.setText(jTable2.getValueAt(fila, 2).toString());
        txtStockActual.setText(jTable2.getValueAt(fila, 3).toString());
        txtStockMinimo.setText(jTable2.getValueAt(fila, 4).toString());
    }

    /** Deja el formulario en blanco y sin insumo seleccionado. */
    private void limpiarCampos() {
        idSeleccionado = 0;                                   // (luiggi) vuelve al modo alta
        txtNombre.setText("");
        txtUnidad.setText("");
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");
        jTable2.clearSelection();
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
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        txtNombre = new javax.swing.JTextField();
        txtUnidad = new javax.swing.JTextField();
        txtStockActual = new javax.swing.JTextField();
        txtStockMinimo = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

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

        jLabel1.setText("Nombre :");

        jLabel2.setText("Unidad :");

        jLabel3.setText("Stock Actual :");

        jLabel4.setText("Stock Minimo :");

        btnGuardar.setText("Guardar");

        btnModificar.setText("Modificar");

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        txtNombre.setText("Nombre");

        txtUnidad.setText("Unidad");

        txtStockActual.setText("StockActual");
        txtStockActual.addActionListener(this::txtStockActualActionPerformed);

        txtStockMinimo.setText("StockMinimo");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Unidad", "Stock Actual", "Stock Minimo"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnGuardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEliminar))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNombre)
                            .addComponent(txtUnidad)
                            .addComponent(txtStockActual)
                            .addComponent(txtStockMinimo))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtStockActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtStockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnModificar)
                    .addComponent(btnEliminar))
                .addGap(55, 55, 55))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

        if (idSeleccionado == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un insumo de la tabla");
            return;                                           // (luiggi) evita borrar sin seleccion
        }

        int confirma = javax.swing.JOptionPane.showConfirmDialog(this,
                "Eliminar el insumo \"" + txtNombre.getText() + "\"?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION); // (luiggi) pide confirmacion antes de borrar

        if (confirma != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        mostrar(control.eliminar(idSeleccionado));   // (luiggi) el controlador decide y explica el resultado
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void txtStockActualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStockActualActionPerformed
        txtStockMinimo.requestFocus();   // (luiggi) Enter salta al siguiente campo
    }//GEN-LAST:event_txtStockActualActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new FrmInsumos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtStockActual;
    private javax.swing.JTextField txtStockMinimo;
    private javax.swing.JTextField txtUnidad;
    // End of variables declaration//GEN-END:variables
}
