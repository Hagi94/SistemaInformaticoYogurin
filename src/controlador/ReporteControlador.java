package controlador;

import Dao.MovimientoInventarioDAO;
import Dao.ReporteDAO;
import javax.swing.table.DefaultTableModel;

/**
 * Controlador de reportes e indicadores (RF-18, RF-19, RF-20, RF-21, RF-27).
 * Coordina los reportes que consume FrmReportes y el dashboard del MenuPrincipal.
 */
public class ReporteControlador {

    private final ReporteDAO dao = new ReporteDAO();
    private final MovimientoInventarioDAO movDao = new MovimientoInventarioDAO();

    // ---------- Reportes de ventas (RF-20) ----------

    public DefaultTableModel ventasDia() {
        return dao.ventasDia();
    }

    public DefaultTableModel ventasSemana() {
        return dao.ventasSemana();
    }

    public DefaultTableModel ventasMes() {
        return dao.ventasMes();
    }

    /** Reporte entre dos fechas, validando el formato y el orden del rango. */
    public Resultado validarRango(String fechaInicio, String fechaFin) {

        if (fechaInicio == null || fechaInicio.isBlank() || fechaFin == null || fechaFin.isBlank()) {
            return Resultado.error("Ingrese la fecha inicial y la final");
        }

        try {
            java.time.LocalDate inicio = java.time.LocalDate.parse(fechaInicio.trim());
            java.time.LocalDate fin = java.time.LocalDate.parse(fechaFin.trim());

            if (inicio.isAfter(fin)) {
                return Resultado.error("La fecha inicial no puede ser posterior a la final"); // (luiggi) rango invertido
            }
            return Resultado.exito("Rango valido");

        } catch (java.time.format.DateTimeParseException e) {
            return Resultado.error("Use el formato de fecha AAAA-MM-DD"); // (luiggi) guia al usuario al formato correcto
        }
    }

    public DefaultTableModel ventasPorRango(String fechaInicio, String fechaFin) {
        return dao.ventasPorRango(fechaInicio, fechaFin);
    }

    // ---------- Produccion e inventario ----------

    public DefaultTableModel reporteProduccion() {
        return dao.reporteProduccion();
    }

    /** Productos bajo su stock minimo (RF-18). */
    public DefaultTableModel stockCritico() {
        return dao.stockCritico();
    }

    /** Insumos bajo su stock minimo (RF-18). */
    public DefaultTableModel insumosCriticos() {
        return dao.insumosCriticos();
    }

    /** Historial de movimientos para auditoria (RF-27). */
    public DefaultTableModel historialInventario() {
        return movDao.listarHistorial();
    }

    // ---------- Cierre de caja (RF-21) ----------

    public DefaultTableModel cierreCaja() {
        return dao.cierreCaja();
    }

    /** Archiva el cierre del dia en la tabla cierre_caja (RF-21). */
    public Resultado guardarCierreCaja(String observacion) {
        return dao.guardarCierreCaja(observacion == null ? "" : observacion.trim())
                ? Resultado.exito("Cierre de caja guardado")
                : Resultado.error("No se pudo guardar el cierre de caja");
    }

    // ---------- Indicadores del dashboard (RF-19) ----------

    /** Total recaudado hoy. */
    public double totalRecaudadoHoy() {
        return dao.totalRecaudadoHoy();
    }

    /** Cantidad de ventas emitidas hoy. */
    public int numeroVentasHoy() {
        DefaultTableModel caja = dao.cierreCaja();
        if (caja.getRowCount() == 0) {
            return 0;                                   // (luiggi) todavia no hubo ventas en el dia
        }
        return Integer.parseInt(caja.getValueAt(0, 1).toString());
    }

    public int cantidadProductosCriticos() {
        return dao.stockCritico().getRowCount();
    }

    public int cantidadInsumosCriticos() {
        return dao.insumosCriticos().getRowCount();
    }
}
