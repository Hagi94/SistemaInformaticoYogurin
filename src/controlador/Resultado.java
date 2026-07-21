package controlador;

/**
 * Resultado de una operacion del controlador.
 * Permite que la vista solo muestre el mensaje, sin decidir si la operacion fue valida.
 */
public class Resultado {

    private final boolean exito;
    private final String mensaje;

    private Resultado(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    /** Operacion realizada correctamente. */
    public static Resultado exito(String mensaje) {
        return new Resultado(true, mensaje);
    }

    /** Operacion rechazada: validacion no superada o fallo al guardar. */
    public static Resultado error(String mensaje) {
        return new Resultado(false, mensaje);
    }

    public boolean esExito() {
        return exito;                       // (luiggi) la vista pregunta esto para elegir el icono del mensaje
    }

    public String getMensaje() {
        return mensaje;
    }
}
