package controlador;

import Dao.ProduccionDAO;
import Modelo.Produccion;
import java.util.List;
import java.util.Map;

/**
 * Controlador de produccion (RF-09, RF-10).
 * Valida los datos del lote y coordina el registro transaccional con descuento de insumos.
 */
public class ProduccionControlador {

    private final ProduccionDAO dao = new ProduccionDAO();

    /**
     * Registra un lote de produccion y descuenta los insumos consumidos (RF-09, RF-10).
     *
     * @param insumosUsados pares idInsumo -> cantidad. Puede ir vacio o nulo.
     */
    public Resultado registrarLote(String fecha, String lote, String sabor,
                                   String cantidad, String precioVenta, String observacion,
                                   Map<Integer, Double> insumosUsados) {

        Produccion p = armar(0, fecha, lote, sabor, cantidad, precioVenta, observacion);
        if (p == null) {
            return Resultado.error(ultimoError);
        }

        int idLote = dao.registrarLote(p, insumosUsados);  // (luiggi) delega la transaccion al DAO

        return idLote > 0
                ? Resultado.exito("Lote " + p.getLote() + " registrado correctamente")
                : Resultado.error("No se registro el lote.\nRevise el stock disponible de los insumos.");
    }

    /** Modifica los datos de un lote existente. */
    public Resultado modificar(int id, String fecha, String lote, String sabor,
                               String cantidad, String precioVenta, String observacion) {

        if (id <= 0) {
            return Resultado.error("Seleccione un lote de la tabla");
        }

        Produccion p = armar(id, fecha, lote, sabor, cantidad, precioVenta, observacion);
        if (p == null) {
            return Resultado.error(ultimoError);
        }

        return dao.modificar(p)
                ? Resultado.exito("Lote actualizado")
                : Resultado.error("No se pudo actualizar el lote");
    }

    /** Elimina un lote y el registro de los insumos que consumio. */
    public Resultado eliminar(int id) {

        if (id <= 0) {
            return Resultado.error("Seleccione un lote de la tabla");
        }

        return dao.eliminar(id)
                ? Resultado.exito("Lote eliminado")
                : Resultado.error("No se pudo eliminar.\nEl lote puede tener ventas registradas.");
    }

    public List<Produccion> listar() {
        return dao.listar();
    }

    public Produccion buscarPorLote(String lote) {
        return lote == null || lote.isBlank() ? null : dao.buscarPorLote(lote.trim());
    }

    public Produccion buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    // ------------------------------------------------------------------

    private String ultimoError;

    /** Valida los campos y arma el objeto. Devuelve null y deja el motivo en ultimoError. */
    private Produccion armar(int id, String fecha, String lote, String sabor,
                             String cantidad, String precioVenta, String observacion) {

        if (fecha == null || fecha.isBlank()) {
            ultimoError = "La fecha del lote es obligatoria";
            return null;
        }
        if (lote == null || lote.isBlank()) {
            ultimoError = "El codigo de lote es obligatorio";
            return null;
        }
        if (sabor == null || sabor.isBlank()) {
            ultimoError = "Indique el sabor producido";
            return null;
        }

        try {
            int unidades = Integer.parseInt(cantidad.trim());
            double precio = Double.parseDouble(precioVenta.trim());

            if (unidades <= 0) {
                ultimoError = "Las unidades producidas deben ser mayores a cero";
                return null;
            }
            if (precio <= 0) {
                ultimoError = "El precio de venta debe ser mayor a cero";
                return null;
            }

            Produccion p = new Produccion();
            p.setId(id);
            p.setFecha(fecha.trim());
            p.setLote(lote.trim());
            p.setSabor(sabor.trim());
            p.setCantidad(unidades);
            p.setPrecioVenta(precio);
            p.setObservacion(observacion == null ? "Ninguna" : observacion.trim());
            return p;

        } catch (NumberFormatException | NullPointerException e) {
            ultimoError = "La cantidad y el precio deben ser numeros";
            return null;                                 // (luiggi) evita que el sistema se caiga con texto
        }
    }
}
