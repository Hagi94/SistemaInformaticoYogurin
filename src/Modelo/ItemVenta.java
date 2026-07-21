package Modelo;

/**
 * Representa una linea del carrito de venta antes de guardarse en la base de datos.
 * Sirve tanto para venta por producto de catalogo como por lote de produccion.
 */
public class ItemVenta {

    public static final String TIPO_PRODUCTO = "PRODUCTO"; // (luiggi) constante en vez de texto suelto
    public static final String TIPO_LOTE = "LOTE";

    private String tipo;              // (luiggi) indica si se vende producto o lote
    private int idItem;               // (luiggi) id del producto o de la produccion segun el tipo
    private String descripcion;       // (luiggi) nombre mostrado en la tabla
    private String lote;              // (luiggi) codigo de lote, solo cuando el tipo es LOTE
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public ItemVenta() {
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    /** Indica si esta linea corresponde a un producto del catalogo. */
    public boolean esProducto() {
        return TIPO_PRODUCTO.equals(tipo);  // (luiggi) evita comparar textos en toda la app
    }
}
