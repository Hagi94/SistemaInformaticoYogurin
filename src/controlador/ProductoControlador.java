package controlador;

import Dao.ProductoDAO;
import Modelo.Producto;
import java.util.List;

/**
 * Controlador de productos (RF-04, RF-05, RF-06).
 * Valida los datos del catalogo y coordina la interaccion con ProductoDAO.
 */
public class ProductoControlador {

    private final ProductoDAO dao = new ProductoDAO();

    /** Registra un producto del catalogo (RF-04). */
    public Resultado registrar(String nombre, String descripcion, String precio,
                               String stock, boolean estado) {

        Producto p = armar(0, nombre, descripcion, precio, stock, estado);
        if (p == null) {
            return Resultado.error(ultimoError);
        }

        return dao.guardar(p)
                ? Resultado.exito("Producto registrado correctamente")
                : Resultado.error("No se pudo registrar el producto");
    }

    /** Modifica o desactiva un producto (RF-05). */
    public Resultado modificar(int id, String nombre, String descripcion, String precio,
                               String stock, boolean estado) {

        if (id <= 0) {
            return Resultado.error("Seleccione un producto de la tabla");
        }

        Producto p = armar(id, nombre, descripcion, precio, stock, estado);
        if (p == null) {
            return Resultado.error(ultimoError);
        }

        return dao.modificar(p)
                ? Resultado.exito("Producto actualizado")
                : Resultado.error("No se pudo actualizar el producto");
    }

    /** Elimina un producto del catalogo. */
    public Resultado eliminar(int id) {

        if (id <= 0) {
            return Resultado.error("Seleccione un producto de la tabla");
        }

        return dao.eliminar(id)
                ? Resultado.exito("Producto eliminado")
                : Resultado.error("No se pudo eliminar.\nEl producto puede tener ventas registradas."); // (luiggi) explica el motivo probable
    }

    /** Busca un producto por su nombre para la pantalla de ventas (RF-06). */
    public Producto buscarPorNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        Producto p = dao.buscarPorNombre(nombre.trim());
        return (p == null || p.getId() == 0) ? null : p;  // (luiggi) evita el NPE que habia en FrmVentas
    }

    public Producto buscarPorId(int id) {
        return dao.buscar(id);
    }

    public List<Producto> listar() {
        return dao.listar();
    }

    // ------------------------------------------------------------------

    private String ultimoError;

    /** Valida los campos y arma el objeto. Devuelve null y deja el motivo en ultimoError. */
    private Producto armar(int id, String nombre, String descripcion,
                           String precio, String stock, boolean estado) {

        if (nombre == null || nombre.isBlank()) {
            ultimoError = "El nombre del producto es obligatorio";
            return null;
        }

        try {
            double valorPrecio = Double.parseDouble(precio.trim());
            int valorStock = Integer.parseInt(stock.trim());

            if (valorPrecio <= 0) {
                ultimoError = "El precio debe ser mayor a cero";
                return null;
            }
            if (valorStock < 0) {
                ultimoError = "El stock no puede ser negativo";
                return null;                             // (luiggi) impide stock imposible desde el formulario
            }

            Producto p = new Producto();
            p.setId(id);
            p.setNombre(nombre.trim());
            p.setDescripcion(descripcion == null ? "" : descripcion.trim());
            p.setPrecio(valorPrecio);
            p.setStock(valorStock);
            p.setEstado(estado);
            return p;

        } catch (NumberFormatException | NullPointerException e) {
            ultimoError = "El precio y el stock deben ser numeros";
            return null;
        }
    }
}
