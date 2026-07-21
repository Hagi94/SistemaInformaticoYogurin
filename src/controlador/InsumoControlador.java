package controlador;

import Dao.InsumoDAO;
import Modelo.Insumo;
import java.util.List;

/**
 * Controlador de insumos (RF-07, RF-08, RF-18).
 * Valida los datos del formulario y coordina la interaccion con InsumoDAO.
 */
public class InsumoControlador {

    private final InsumoDAO dao = new InsumoDAO();

    /** Registra un insumo nuevo (RF-07). */
    public Resultado registrar(String nombre, String unidad, String stockActual, String stockMinimo) {

        Insumo i = armar(0, nombre, unidad, stockActual, stockMinimo);
        if (i == null) {
            return Resultado.error(ultimoError);        // (luiggi) el detalle lo dejo el metodo armar()
        }

        return dao.guardar(i)
                ? Resultado.exito("Insumo registrado correctamente")
                : Resultado.error("No se pudo registrar el insumo");
    }

    /** Modifica un insumo existente. */
    public Resultado modificar(int id, String nombre, String unidad,
                               String stockActual, String stockMinimo) {

        if (id <= 0) {
            return Resultado.error("Seleccione un insumo de la tabla");
        }

        Insumo i = armar(id, nombre, unidad, stockActual, stockMinimo);
        if (i == null) {
            return Resultado.error(ultimoError);
        }

        return dao.modificar(i)
                ? Resultado.exito("Insumo actualizado")
                : Resultado.error("No se pudo actualizar el insumo");
    }

    /** Elimina un insumo del catalogo. */
    public Resultado eliminar(int id) {

        if (id <= 0) {
            return Resultado.error("Seleccione un insumo de la tabla");
        }

        return dao.eliminar(id)
                ? Resultado.exito("Insumo eliminado")
                : Resultado.error("No se pudo eliminar.\nEl insumo puede estar usado en un lote de produccion.");
    }

    /** Suma al stock la cantidad comprada de un insumo (RF-08). */
    public Resultado registrarEntrada(int id, String textoCantidad) {

        if (id <= 0) {
            return Resultado.error("Seleccione primero el insumo comprado");
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(textoCantidad.trim()); // (luiggi) valida el texto del cuadro de dialogo
        } catch (NumberFormatException | NullPointerException e) {
            return Resultado.error("Ingrese un numero valido");
        }

        if (cantidad <= 0) {
            return Resultado.error("La cantidad debe ser mayor a cero");
        }

        return dao.registrarEntrada(id, cantidad)
                ? Resultado.exito("Entrada registrada")
                : Resultado.error("No se pudo registrar la entrada");
    }

    /** Lista completa de insumos. */
    public List<Insumo> listar() {
        return dao.listar();
    }

    /** Insumos que llegaron o bajaron de su stock minimo (RF-18). */
    public List<Insumo> listarStockCritico() {
        return dao.listarStockCritico();
    }

    public Insumo buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    // ------------------------------------------------------------------

    private String ultimoError;

    /** Valida los campos y arma el objeto. Devuelve null y deja el motivo en ultimoError. */
    private Insumo armar(int id, String nombre, String unidad,
                         String stockActual, String stockMinimo) {

        if (nombre == null || nombre.isBlank() || unidad == null || unidad.isBlank()) {
            ultimoError = "El nombre y la unidad son obligatorios";
            return null;
        }

        try {
            double actual = Double.parseDouble(stockActual.trim());
            double minimo = Double.parseDouble(stockMinimo.trim());

            if (actual < 0 || minimo < 0) {
                ultimoError = "Los stocks no pueden ser negativos";
                return null;                            // (luiggi) impide guardar cantidades imposibles
            }

            Insumo i = new Insumo();
            i.setId(id);
            i.setNombre(nombre.trim());
            i.setUnidad(unidad.trim());
            i.setStockActual(actual);
            i.setStockMinimo(minimo);
            return i;

        } catch (NumberFormatException | NullPointerException e) {
            ultimoError = "Los stocks deben ser numeros";
            return null;                                // (luiggi) evita que el sistema se caiga con texto
        }
    }
}
