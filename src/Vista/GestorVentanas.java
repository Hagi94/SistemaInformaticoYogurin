package Vista;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JFrame;

/**
 * Controla las ventanas abiertas del sistema.
 *
 * Sin esto, cada clic en un boton del menu creaba una ventana nueva: se podian tener
 * varias pantallas de Ventas descontando stock a la vez. Ahora, si la ventana ya esta
 * abierta, se trae al frente en lugar de duplicarla.
 */
public class GestorVentanas {

    private static final Map<String, JFrame> ABIERTAS = new HashMap<>();

    private GestorVentanas() {                       // (luiggi) clase de utilidad, no se instancia
    }

    /**
     * Muestra la ventana identificada por la clave dada.
     * Si ya estaba abierta la trae al frente; si no, la crea con el proveedor recibido.
     *
     * @param clave    identificador de la ventana, por ejemplo "ventas"
     * @param creador  como construir la ventana la primera vez
     */
    public static void abrir(String clave, Supplier<JFrame> creador) {

        JFrame ventana = ABIERTAS.get(clave);

        if (ventana != null && ventana.isDisplayable()) {

            if (ventana.getState() == JFrame.ICONIFIED) {
                ventana.setState(JFrame.NORMAL);     // (luiggi) la restaura si estaba minimizada
            }
            ventana.toFront();
            ventana.requestFocus();                  // (luiggi) reutiliza la que ya existe
            return;
        }

        ventana = creador.get();                     // (luiggi) no existe o fue cerrada: se crea
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // (luiggi) cerrarla no apaga el sistema
        ABIERTAS.put(clave, ventana);
        ventana.setVisible(true);
    }

    /** Cierra todas las ventanas abiertas. Se usa al cerrar sesion. */
    public static void cerrarTodas() {

        for (JFrame ventana : ABIERTAS.values()) {
            if (ventana != null && ventana.isDisplayable()) {
                ventana.dispose();                   // (luiggi) no deja pantallas de otro usuario abiertas
            }
        }
        ABIERTAS.clear();
    }
}
