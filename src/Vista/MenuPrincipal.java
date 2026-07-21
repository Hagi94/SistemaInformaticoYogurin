/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;


public class MenuPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MenuPrincipal.class.getName());

    /**
     * Creates new form MenuPrincipal
     */
    public MenuPrincipal() {
        initComponents();
        btnInsumos.setText("Insumos");
        btnUsuarios.addActionListener(this::btnUsuariosActionPerformed);
        btnProductos.addActionListener(this::btnProductosActionPerformed);
        btnClientes.addActionListener(this::btnClientesActionPerformed);
        btnInsumos.addActionListener(this::btnInsumosActionPerformed);
        btnProduccion.addActionListener(this::btnProduccionActionPerformed);
        btnVentas.addActionListener(this::btnVentasActionPerformed);
        btnInventario.addActionListener(this::btnInventarioActionPerformed);
        btnReportes.addActionListener(this::btnReportesActionPerformed);
        btnCerrarSesion.addActionListener(this::btnCerrarSesionActionPerformed);

        aplicarPermisosPorRol();   // (luiggi) habilita o bloquea botones segun el rol del usuario
        construirDashboard();      // (luiggi) arma el panel de indicadores (RF-19)
        refrescarIndicadores();
        construirMenuRespaldo();   // (luiggi) agrega el menu de respaldo de datos (RF-22)
    }

    // ------------------------------------------------------------------
    // Respaldo y restauracion de la base de datos (RF-22)
    // ------------------------------------------------------------------

    /** Menu superior con las opciones de respaldo, visible solo para el Administrador. */
    private void construirMenuRespaldo() {

        if (!util.Sesion.esAdministrador()) {
            return;                                          // (luiggi) el vendedor no respalda la base
        }

        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu menu = new javax.swing.JMenu("Base de datos");

        javax.swing.JMenuItem itemCrear = new javax.swing.JMenuItem("Crear respaldo...");
        itemCrear.addActionListener(e -> crearRespaldo());

        javax.swing.JMenuItem itemRestaurar = new javax.swing.JMenuItem("Restaurar respaldo...");
        itemRestaurar.addActionListener(e -> restaurarRespaldo());

        menu.add(itemCrear);
        menu.addSeparator();
        menu.add(itemRestaurar);
        barra.add(menu);
        setJMenuBar(barra);                                  // (luiggi) el menu no altera el layout del formulario
    }

    /** Genera una copia de seguridad de la base de datos en un archivo .sql (RF-22). */
    private void crearRespaldo() {

        javax.swing.JFileChooser selector = new javax.swing.JFileChooser(respaldoControl.carpetaRespaldos());
        selector.setDialogTitle("Guardar respaldo de la base de datos");
        selector.setSelectedFile(new java.io.File(respaldoControl.nombreSugerido())); // (luiggi) propone nombre con fecha

        if (selector.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;                                          // (luiggi) el usuario cancelo
        }

        java.io.File destino = selector.getSelectedFile();

        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)); // (luiggi) avisa que esta trabajando
        controlador.Resultado r = respaldoControl.crear(destino);  // (luiggi) el controlador verifica permisos y ejecuta
        setCursor(java.awt.Cursor.getDefaultCursor());

        mostrar(r);
    }

    /** Muestra el mensaje del controlador con el icono que corresponda. */
    private void mostrar(controlador.Resultado r) {
        javax.swing.JOptionPane.showMessageDialog(this, r.getMensaje(),
                r.esExito() ? "Listo" : "Error",
                r.esExito() ? javax.swing.JOptionPane.INFORMATION_MESSAGE
                            : javax.swing.JOptionPane.ERROR_MESSAGE); // (luiggi) el icono depende del resultado
    }

    /** Restaura la base de datos desde un archivo de respaldo previo (RF-22). */
    private void restaurarRespaldo() {

        javax.swing.JFileChooser selector = new javax.swing.JFileChooser(respaldoControl.carpetaRespaldos());
        selector.setDialogTitle("Seleccione el archivo de respaldo (.sql)");

        if (selector.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File origen = selector.getSelectedFile();

        int confirma = javax.swing.JOptionPane.showConfirmDialog(this,
                respaldoControl.textoConfirmacion(origen),   // (luiggi) el texto de advertencia lo define el controlador
                "Confirmar restauracion",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);    // (luiggi) doble aviso por ser una accion destructiva

        if (confirma != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        controlador.Resultado r = respaldoControl.restaurar(origen);
        setCursor(java.awt.Cursor.getDefaultCursor());

        mostrar(r);

        if (r.esExito()) {
            refrescarIndicadores();                          // (luiggi) recalcula el dashboard con los datos nuevos
        }
    }

    // ------------------------------------------------------------------
    // Dashboard de indicadores (RF-19)
    // ------------------------------------------------------------------

    private static final java.awt.Color COLOR_OK = new java.awt.Color(232, 245, 233);
    private static final java.awt.Color COLOR_ALERTA = new java.awt.Color(255, 228, 228);

    private final controlador.ReporteControlador reporteControl = new controlador.ReporteControlador();
    private final controlador.RespaldoControlador respaldoControl = new controlador.RespaldoControlador(); // (luiggi) la vista solo habla con controladores

    private javax.swing.JLabel lblVentasHoy;
    private javax.swing.JLabel lblNumVentas;
    private javax.swing.JLabel lblStockProductos;
    private javax.swing.JLabel lblStockInsumos;

    /**
     * Construye el panel de indicadores a la derecha del menu.
     * Se agrega por codigo para no alterar el formulario generado por NetBeans.
     */
    private void construirDashboard() {

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(5, 1, 0, 8));
        panel.setOpaque(true);
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Resumen del dia"));

        lblVentasHoy      = crearIndicador();
        lblNumVentas      = crearIndicador();
        lblStockProductos = crearIndicador();
        lblStockInsumos   = crearIndicador();

        panel.add(lblVentasHoy);
        panel.add(lblNumVentas);
        panel.add(lblStockProductos);
        panel.add(lblStockInsumos);

        javax.swing.JButton btnActualizar = new javax.swing.JButton("Actualizar");
        btnActualizar.addActionListener(e -> refrescarIndicadores()); // (luiggi) recalcula los indicadores
        panel.add(btnActualizar);

        // El formulario mide 380x300, por eso el panel se coloca a partir de x=390
        getContentPane().add(panel,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 250, 260));

        setSize(660, 340);                                    // (luiggi) agranda la ventana para el panel
        setLocationRelativeTo(null);                          // (luiggi) centra el menu en la pantalla
    }

    /** Crea una etiqueta con el formato visual de los indicadores. */
    private javax.swing.JLabel crearIndicador() {
        javax.swing.JLabel lbl = new javax.swing.JLabel("", javax.swing.SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(COLOR_OK);
        lbl.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
        return lbl;
    }

    /** Consulta la base de datos y actualiza los cuatro indicadores. */
    private void refrescarIndicadores() {

        double total = reporteControl.totalRecaudadoHoy();         // (luiggi) suma de ventas del dia
        lblVentasHoy.setText("Ventas de hoy:  S/ " + String.format("%.2f", total));

        int numVentas = reporteControl.numeroVentasHoy();      // (luiggi) el conteo lo resuelve el controlador
        lblNumVentas.setText("Comprobantes emitidos:  " + numVentas);

        int prodCriticos = reporteControl.cantidadProductosCriticos();  // (luiggi) productos bajo el minimo
        lblStockProductos.setText("Productos en stock critico:  " + prodCriticos);
        lblStockProductos.setBackground(prodCriticos > 0 ? COLOR_ALERTA : COLOR_OK);

        int insCriticos = reporteControl.cantidadInsumosCriticos();     // (luiggi) insumos bajo el minimo
        lblStockInsumos.setText("Insumos en stock critico:  " + insCriticos);
        lblStockInsumos.setBackground(insCriticos > 0 ? COLOR_ALERTA : COLOR_OK);
    }

    /**
     * Control de acceso por rol (RF-02).
     * El Administrador tiene acceso total; el Vendedor solo a Ventas, Clientes e Inventario.
     */
    private void aplicarPermisosPorRol() {

        if (!util.Sesion.haySesion()) {                      // (luiggi) nadie debe entrar sin iniciar sesion
            javax.swing.JOptionPane.showMessageDialog(this, "Debe iniciar sesion");
            this.dispose();
            new LoginForm().setVisible(true);
            return;
        }

        boolean esAdmin = util.Sesion.esAdministrador();      // (luiggi) consulta el rol guardado en la sesion

        btnUsuarios.setEnabled(esAdmin);                     // (luiggi) solo el admin gestiona usuarios
        btnProductos.setEnabled(esAdmin);                    // (luiggi) solo el admin edita el catalogo
        btnInsumos.setEnabled(esAdmin);                      // (luiggi) solo el admin controla insumos
        btnProduccion.setEnabled(esAdmin);                   // (luiggi) solo el admin registra lotes
        btnReportes.setEnabled(esAdmin);                     // (luiggi) los reportes son gerenciales

        setTitle("Yogurin Bustamante - " + util.Sesion.getNombreUsuario()
                + " (" + util.Sesion.getUsuarioActivo().getRol() + ")"); // (luiggi) muestra quien esta logueado
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnUsuarios = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnInsumos = new javax.swing.JButton();
        btnProduccion = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnReportes = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        lgoInicial = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnUsuarios.setText("Usuarios");
        getContentPane().add(btnUsuarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 29, 91, -1));

        btnProductos.setText("Productos");
        btnProductos.addActionListener(this::btnProductosActionPerformed);
        getContentPane().add(btnProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 91, -1));

        btnInsumos.setText("Insumos");
        btnInsumos.addActionListener(this::btnInsumosActionPerformed);
        getContentPane().add(btnInsumos, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 133, 91, -1));

        btnProduccion.setText("Produccion");
        getContentPane().add(btnProduccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 191, -1, -1));

        btnClientes.setText("Clientes");
        getContentPane().add(btnClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 29, 83, -1));

        btnVentas.setText("Ventas");
        getContentPane().add(btnVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 80, 83, -1));

        btnInventario.setText("Inventario");
        getContentPane().add(btnInventario, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 133, -1, -1));

        btnReportes.setText("Reportes");
        getContentPane().add(btnReportes, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 191, 83, -1));

        btnCerrarSesion.setText("Cerrar Sesion");
        getContentPane().add(btnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(118, 232, -1, -1));

        lgoInicial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Botones/LogoPrincipal.png"))); // NOI18N
        getContentPane().add(lgoInicial, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 380, 300));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnInsumosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsumosActionPerformed
        GestorVentanas.abrir("insumos", FrmInsumos::new);   // (luiggi) reutiliza la ventana si ya esta abierta
    }//GEN-LAST:event_btnInsumosActionPerformed

    private void btnProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductosActionPerformed
GestorVentanas.abrir("productos", FrmProductos::new);
    }//GEN-LAST:event_btnProductosActionPerformed
private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.abrir("usuarios", FrmUsuarios::new);

}


private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.abrir("clientes", FrmClientes::new);

}

private void btnProduccionActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.abrir("produccion", FrmProduccion::new);

}

private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.abrir("ventas", FrmVentas::new);

}

private void btnInventarioActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.abrir("inventario", FrmInventario::new);

}

private void btnReportesActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.abrir("reportes", FrmReportes::new);

}

private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {

    GestorVentanas.cerrarTodas();                        // (luiggi) cierra las pantallas del usuario que sale
    new controlador.UsuarioControlador().cerrarSesion();  // (luiggi) borra el usuario activo al salir (RF-03)

    this.dispose();

    new LoginForm().setVisible(true);

}
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnInsumos;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnProduccion;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnReportes;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVentas;
    private javax.swing.JLabel lgoInicial;
    // End of variables declaration//GEN-END:variables
}
