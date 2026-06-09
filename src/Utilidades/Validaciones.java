package Utilidades;

public final class Validaciones {

    private Validaciones() {
    }

    public static boolean noEsVacio(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    public static boolean esEmailValido(String email) {
        return noEsVacio(email) && email.matches("^[\\w.!#$%&’*+/=?`{|}~-]+@[\\w-]+(?:\\.[\\w-]+)+$");
    }

    public static boolean esDniValido(String dni) {
        return noEsVacio(dni) && dni.matches("^\\d{8}$");
    }

    public static boolean esTelefonoValido(String telefono) {
        return noEsVacio(telefono) && telefono.matches("^[0-9+\\-\\s]{7,15}$");
    }

    public static boolean esNumeroPositivo(double numero) {
        return numero > 0;
    }
}
