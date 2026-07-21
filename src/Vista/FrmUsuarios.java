/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import controlador.UsuarioControlador;
import Modelo.Usuario;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class FrmUsuarios extends javax.swing.JFrame {
    
    UsuarioControlador control = new UsuarioControlador(); // (luiggi) la vista solo habla con el controlador
    DefaultTableModel modelo = new DefaultTableModel();
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmUsuarios.class.getName());

    /**
     * Creates new form FrmUsuarios
     */
    public FrmUsuarios() {
        initComponents();
        txtId.setEditable(false);
        listarUsuarios();

        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        btnModificar.addActionListener(this::btnModificarActionPerformed);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
    }

  
    // Se elimino initComponents2(): era una copia del diseno que nunca se invocaba

    // ========== MÉTODOS ==========

    public void listarUsuarios() {
        modelo = (DefaultTableModel) tblUsuarios.getModel();
        modelo.setRowCount(0);

        List<Usuario> lista = control.listar();

        for (Usuario u : lista) {
            Object datos[] = {
                u.getId(),
                u.getUsuario(),
                "********",          // (luiggi) nunca se muestra el hash de la clave en pantalla
                u.getRol(),
                u.isEstado()
            };
            modelo.addRow(datos);
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        txtClave = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cmbRol = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        chkEstado = new javax.swing.JCheckBox();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        btnRegresar = new javax.swing.JButton();
        lgoUsuario = new javax.swing.JLabel();

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

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jTable2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("ID :");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 47, 37, -1));
        getContentPane().add(txtId, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 44, 180, -1));

        jLabel2.setText("Usuario :");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 75, 57, -1));

        jLabel3.setText("Contrasena :");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 109, 80, -1));
        getContentPane().add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 72, 180, -1));
        getContentPane().add(txtClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 106, 180, -1));

        jLabel4.setText("Rol :");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 149, 37, -1));

        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Vendedor" }));
        cmbRol.addItemListener(this::cmbRolItemStateChanged);
        cmbRol.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                cmbRolComponentHidden(evt);
            }
        });
        cmbRol.addActionListener(this::cmbRolActionPerformed);
        getContentPane().add(cmbRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 146, 180, -1));

        jLabel5.setText("Estado :");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 188, 57, -1));

        chkEstado.setText("Activo");
        getContentPane().add(chkEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 186, -1, -1));

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        getContentPane().add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 248, -1, -1));

        btnModificar.setText("Modificar");
        btnModificar.addActionListener(this::btnModificarActionPerformed);
        getContentPane().add(btnModificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 248, -1, -1));

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        getContentPane().add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(206, 248, -1, -1));

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        getContentPane().add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(291, 248, -1, -1));

        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "USUARIO", "CLAVE", "ROL", "ESTADO"
            }
        ));
        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblUsuarios);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 6, -1, 310));

        btnRegresar.setText("Regresar");
        btnRegresar.addActionListener(this::btnRegresarActionPerformed);
        getContentPane().add(btnRegresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(369, 248, -1, -1));

        lgoUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Botones/LogoNaranja.png"))); // NOI18N
        getContentPane().add(lgoUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1230, 350));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbRolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRolActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbRolActionPerformed

    private void cmbRolComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_cmbRolComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbRolComponentHidden

    private void cmbRolItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbRolItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbRolItemStateChanged

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_formMouseClicked

    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsuariosMouseClicked
int fila = tblUsuarios.getSelectedRow();

        if (fila >= 0) {
            txtId.setText(tblUsuarios.getValueAt(fila, 0).toString());
            txtUsuario.setText(tblUsuarios.getValueAt(fila, 1).toString());
            txtClave.setText("");   // (luiggi) se deja vacia: en blanco significa conservar la clave actual
            cmbRol.setSelectedItem(tblUsuarios.getValueAt(fila, 3).toString());
            chkEstado.setSelected(Boolean.parseBoolean(tblUsuarios.getValueAt(fila, 4).toString()));
        }
    
    }//GEN-LAST:event_tblUsuariosMouseClicked

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed

        // La vista entrega el texto; el controlador valida y cifra la clave
        mostrar(control.registrar(                            // (luiggi) alta de usuario (RF-02)
                txtUsuario.getText(),
                txtClave.getText(),
                cmbRol.getSelectedItem().toString(),
                chkEstado.isSelected()), evt);
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed

        mostrar(control.modificar(idSeleccionado(),
                txtUsuario.getText(),
                txtClave.getText(),                           // (luiggi) vacia = conserva la clave actual
                cmbRol.getSelectedItem().toString(),
                chkEstado.isSelected()), evt);
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

        if (idSeleccionado() == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario primero");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar este usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            mostrar(control.eliminar(idSeleccionado()), evt);  // (luiggi) el controlador impide borrar la propia cuenta
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    /** Id del usuario seleccionado en la tabla, o 0 si no hay ninguno. */
    private int idSeleccionado() {
        String texto = txtId.getText();
        return texto == null || texto.isBlank() ? 0 : Integer.parseInt(texto.trim());
    }

    /** Muestra el mensaje del controlador y refresca la tabla si la operacion salio bien. */
    private void mostrar(controlador.Resultado r, java.awt.event.ActionEvent evt) {

        JOptionPane.showMessageDialog(this, r.getMensaje(),
                r.esExito() ? "Listo" : "Atencion",
                r.esExito() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        if (r.esExito()) {
            listarUsuarios();
            btnLimpiarActionPerformed(evt);                   // (luiggi) deja el formulario listo para el siguiente
        }
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
         txtId.setText("");
        txtUsuario.setText("");
        txtClave.setText("");
        cmbRol.setSelectedIndex(0);
        chkEstado.setSelected(false);
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        // El menu principal sigue abierto detras; crear otro lo duplicaba
        this.dispose();   // (luiggi) solo cierra esta ventana y vuelve al menu
    }//GEN-LAST:event_btnRegresarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JCheckBox chkEstado;
    private javax.swing.JComboBox<String> cmbRol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel lgoUsuario;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtClave;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
