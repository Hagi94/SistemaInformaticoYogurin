package controlador;

import Dao.VentaDAO;
import Modelo.ItemVenta;
import Modelo.Venta;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de ventas (RF-13, RF-14, RF-15, RF-25).
 * Mantiene el carrito en memoria, calcula los totales y coordina el registro transaccional.
 */
public class VentaControlador {

    private final VentaDAO dao = new VentaDAO();
    private final List<ItemVenta> carrito = new ArrayList<>();  // (luiggi) lineas de la venta en curso

    /** Agrega una linea al carrito validando cantidad y precio (RF-13). */
    public Resultado agregarItem(String tipo, int idItem, String descripcion,
                                 String lote, String textoCantidad, double precio) {

        if (idItem <= 0) {
            return Resultado.error("Seleccione un producto o lote");
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad.trim());   // (luiggi) convierte el texto escrito por el usuario
        } catch (NumberFormatException | NullPointerException e) {
            return Resultado.error("La cantidad debe ser un numero entero");
        }

        if (cantidad <= 0) {
            return Resultado.error("La cantidad debe ser mayor a cero");
        }
        if (precio <= 0) {
            return Resultado.error("El precio no es valido");
        }

        ItemVenta item = new ItemVenta();
        item.setTipo(tipo);
        item.setIdItem(idItem);
        item.setDescripcion(descripcion);
        item.setLote(lote);
        item.setCantidad(cantidad);
        item.setPrecioUnitario(precio);
        item.setSubtotal(cantidad * precio);                    // (luiggi) calculo automatico del subtotal (RF-14)

        carrito.add(item);
        return Resultado.exito("Item agregado");
    }

    /**
     * Agrega un producto del catalogo al carrito, verificando el stock disponible.
     * La vista solo entrega el producto elegido y el texto de la cantidad.
     */
    public Resultado agregarProducto(Modelo.Producto p, String textoCantidad) {

        if (p == null) {
            return Resultado.error("Seleccione un producto"); // (luiggi) evita el NPE que habia antes
        }

        Resultado validacion = validarCantidad(textoCantidad, p.getStock(), "Stock disponible: ");
        if (!validacion.esExito()) {
            return validacion;
        }

        return agregarItem(ItemVenta.TIPO_PRODUCTO, p.getId(), p.getNombre(),
                null, textoCantidad, p.getPrecio());
    }

    /** Agrega un lote de produccion al carrito, verificando las unidades disponibles. */
    public Resultado agregarLote(Modelo.Produccion prod, String textoCantidad) {

        if (prod == null) {
            return Resultado.error("Seleccione un lote");
        }

        Resultado validacion = validarCantidad(textoCantidad, prod.getCantidad(), "Unidades disponibles: ");
        if (!validacion.esExito()) {
            return validacion;
        }

        String descripcion = prod.getSabor() + " (Lote: " + prod.getLote() + ")";

        return agregarItem(ItemVenta.TIPO_LOTE, prod.getId(), descripcion,
                prod.getLote(), textoCantidad, prod.getPrecioVenta());
    }

    /** Comprueba que la cantidad escrita sea un entero positivo y no supere lo disponible. */
    private Resultado validarCantidad(String texto, int disponible, String etiqueta) {

        int cantidad;
        try {
            cantidad = Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return Resultado.error("La cantidad debe ser un numero entero");
        }

        if (cantidad <= 0) {
            return Resultado.error("La cantidad debe ser mayor a cero");
        }
        if (cantidad > disponible) {
            return Resultado.error("Stock insuficiente. " + etiqueta + disponible); // (luiggi) avisa antes de llegar a la BD
        }
        return Resultado.exito("");
    }

    /** Suma de todos los subtotales del carrito (RF-14). */
    public double getTotal() {
        return carrito.stream().mapToDouble(ItemVenta::getSubtotal).sum();
    }

    /** Valida el descuento escrito y devuelve el total a pagar. */
    public double calcularTotalPagar(double descuento) {
        return getTotal() - descuento;
    }

    /** Convierte el texto del descuento a numero, validandolo contra el total. */
    public Resultado validarDescuento(String texto) {

        double descuento;
        try {
            descuento = (texto == null || texto.isBlank()) ? 0 : Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return Resultado.error("El descuento debe ser un numero");
        }

        if (descuento < 0) {
            return Resultado.error("El descuento no puede ser negativo");
        }
        if (descuento > getTotal()) {
            return Resultado.error("El descuento no puede ser mayor al total"); // (luiggi) evita totales negativos
        }
        return Resultado.exito(String.valueOf(descuento));
    }

    /**
     * Registra la venta completa dentro de una transaccion (RF-15).
     * El id generado queda disponible en getUltimoIdVenta().
     */
    public Resultado registrarVenta(int idCliente, double descuento) {

        if (carrito.isEmpty()) {
            return Resultado.error("Agregue productos a la venta");
        }
        if (idCliente <= 0) {
            return Resultado.error("Seleccione un cliente");
        }

        double total = getTotal();

        Venta v = new Venta();
        v.setClienteId(idCliente);
        v.setTotal(total);
        v.setDescuento(descuento);
        v.setTotalPagar(total - descuento);

        ultimoIdVenta = dao.registrarVenta(v, carrito);         // (luiggi) delega la transaccion al DAO

        if (ultimoIdVenta <= 0) {
            return Resultado.error("No se registro la venta.\nRevise el stock disponible de los productos.");
        }

        carrito.clear();                                        // (luiggi) deja el carrito listo para la proxima venta
        return Resultado.exito("Venta N " + ultimoIdVenta + " registrada correctamente"
                + "\nTotal: S/ " + String.format("%.2f", total - descuento));
    }

    /** Anula una venta registrada por error y devuelve el stock (RF-25). */
    public Resultado anularVenta(int idVenta) {

        if (idVenta <= 0) {
            return Resultado.error("Indique el numero de venta a anular");
        }

        return dao.anularVenta(idVenta)
                ? Resultado.exito("Venta anulada y stock devuelto")
                : Resultado.error("No se pudo anular la venta N " + idVenta);
    }

    /** Vacia el carrito sin registrar nada. */
    public void cancelarVenta() {
        carrito.clear();
    }

    /** Quita una linea del carrito por su posicion en la tabla. */
    public Resultado quitarItem(int indice) {
        if (indice < 0 || indice >= carrito.size()) {
            return Resultado.error("Seleccione una linea de la tabla");
        }
        carrito.remove(indice);
        return Resultado.exito("Item quitado");
    }

    /** Lineas actuales del carrito, para que la vista las pinte. */
    public List<ItemVenta> getCarrito() {
        return carrito;
    }

    public boolean carritoVacio() {
        return carrito.isEmpty();
    }

    private int ultimoIdVenta = 0;

    public int getUltimoIdVenta() {
        return ultimoIdVenta;
    }
}
