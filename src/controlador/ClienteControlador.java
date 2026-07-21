package controlador;

import Dao.ClienteDAO;
import Modelo.Cliente;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Controlador de clientes (RF-11, RF-12, RF-26).
 * Valida los datos del formulario y coordina la interaccion con ClienteDAO.
 */
public class ClienteControlador {

    private static final int LARGO_DNI = 8;   // (luiggi) el DNI peruano tiene 8 digitos

    private final ClienteDAO dao = new ClienteDAO();

    /** Registra un cliente nuevo (RF-11). */
    public Resultado registrar(String dni, String nombre, String telefono,
                               String direccion, String correo) {

        String problema = validar(dni, nombre, correo);
        if (problema != null) {
            return Resultado.error(problema);
        }

        return dao.guardar(armar(0, dni, nombre, telefono, direccion, correo))
                ? Resultado.exito("Cliente registrado correctamente")
                : Resultado.error("No se pudo registrar.\nEs posible que el DNI ya este registrado."); // (luiggi) el DNI es UNIQUE

    }

    /** Modifica un cliente existente. */
    public Resultado modificar(int id, String dni, String nombre, String telefono,
                               String direccion, String correo) {

        if (id <= 0) {
            return Resultado.error("Seleccione un cliente de la tabla");
        }

        String problema = validar(dni, nombre, correo);
        if (problema != null) {
            return Resultado.error(problema);
        }

        return dao.modificar(armar(id, dni, nombre, telefono, direccion, correo))
                ? Resultado.exito("Cliente actualizado")
                : Resultado.error("No se pudo actualizar el cliente");
    }

    /** Elimina un cliente, siempre que no tenga ventas registradas. */
    public Resultado eliminar(int id) {

        if (id <= 0) {
            return Resultado.error("Seleccione un cliente de la tabla");
        }

        if (dao.contarCompras(id) > 0) {
            return Resultado.error("No se puede eliminar: el cliente tiene ventas registradas"); // (luiggi) protege la integridad referencial
        }

        return dao.eliminar(id)
                ? Resultado.exito("Cliente eliminado")
                : Resultado.error("No se pudo eliminar el cliente");
    }

    /** Historial de compras del cliente (RF-26). */
    public DefaultTableModel historialCompras(int idCliente) {
        return dao.historialCompras(idCliente);
    }

    public int contarCompras(int idCliente) {
        return dao.contarCompras(idCliente);
    }

    /** Busca por DNI o por nombre, segun lo que escriba el vendedor (RF-12). */
    public Cliente buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        String dato = texto.trim();
        Cliente c = dato.matches("\\d+")               // (luiggi) si son solo digitos, busca por DNI
                ? dao.buscarPorDni(dato)
                : dao.buscarPorNombre(dato);

        return (c == null || c.getId() == 0) ? null : c; // (luiggi) el DAO devuelve un objeto vacio si no encuentra
    }

    public Cliente buscarPorNombre(String nombre) {
        return dao.buscarPorNombre(nombre);
    }

    public List<Cliente> listar() {
        return dao.listar();
    }

    // ------------------------------------------------------------------

    /** Reglas de validacion. Devuelve null si los datos son correctos. */
    private String validar(String dni, String nombre, String correo) {

        if (nombre == null || nombre.isBlank()) {
            return "El nombre del cliente es obligatorio";
        }

        if (dni != null && !dni.isBlank()) {
            String limpio = dni.trim();
            if (!limpio.matches("\\d{" + LARGO_DNI + "}")) {
                return "El DNI debe tener exactamente " + LARGO_DNI + " digitos";
            }
        }

        if (correo != null && !correo.isBlank() && !correo.contains("@")) {
            return "El correo no tiene un formato valido";  // (luiggi) validacion basica de correo
        }
        return null;
    }

    /** Arma el objeto Cliente a partir de los campos del formulario. */
    private Cliente armar(int id, String dni, String nombre, String telefono,
                          String direccion, String correo) {

        Cliente c = new Cliente();
        c.setId(id);
        c.setDni(dni == null || dni.isBlank() ? null : dni.trim()); // (luiggi) null en vez de vacio por el UNIQUE
        c.setNombre(nombre.trim());
        c.setTelefono(telefono == null ? "" : telefono.trim());
        c.setDireccion(direccion == null ? "" : direccion.trim());
        c.setCorreo(correo == null ? "" : correo.trim());
        return c;
    }
}
