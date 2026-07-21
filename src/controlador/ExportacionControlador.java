package controlador;

import Dao.VentaDAO;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;
import util.ExportadorCSV;
import util.GeneradorPDF;

/**
 * Controlador de exportacion de documentos (RF-16, RF-24).
 * Coordina la generacion del comprobante de venta y la exportacion de reportes.
 */
public class ExportacionControlador {

    /** Carpeta donde se guardan los documentos generados. */
    public static final String CARPETA = "documentos";

    private final VentaDAO ventaDao = new VentaDAO();

    /** Carpeta de documentos; la crea si todavia no existe. */
    public File carpetaDocumentos() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();                                // (luiggi) crea la carpeta la primera vez
        }
        return carpeta;
    }

    /** Nombre sugerido para el comprobante de una venta. */
    public String nombreComprobante(int idVenta) {
        return "comprobante_venta_" + idVenta + ".pdf";
    }

    /** Nombre sugerido para un reporte exportado, con fecha y hora. */
    public String nombreReporte(String titulo, String extension) {

        String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));

        // Separa cada letra de su tilde y luego descarta las tildes: "Día" queda "dia", no "da"
        String sinTildes = java.text.Normalizer
                .normalize(titulo, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // (luiggi) conserva la letra base

        String limpio = sinTildes.toLowerCase()
                .replace(" ", "_")
                .replaceAll("[^a-z0-9_]", "");

        return limpio + "_" + marca + "." + extension;
    }

    /**
     * Genera el comprobante en PDF de una venta ya registrada (RF-16).
     */
    public Resultado generarComprobante(File destino, int idVenta) {

        if (idVenta <= 0) {
            return Resultado.error("Indique el numero de venta");
        }
        if (destino == null) {
            return Resultado.error("Indique donde guardar el comprobante");
        }

        String[] cabecera = ventaDao.cabeceraComprobante(idVenta);

        if (cabecera == null) {
            return Resultado.error("No existe la venta N " + idVenta); // (luiggi) pudo haber sido anulada
        }

        DefaultTableModel detalle = ventaDao.detalleComprobante(idVenta);

        if (detalle.getRowCount() == 0) {
            return Resultado.error("La venta N " + idVenta + " no tiene detalle");
        }

        String error = GeneradorPDF.generarComprobante(destino, cabecera, detalle);

        return error.isEmpty()
                ? Resultado.exito("Comprobante generado:\n" + destino.getAbsolutePath())
                : Resultado.error(error);
    }

    /** Exporta un reporte de pantalla a PDF (RF-24). */
    public Resultado exportarPdf(File destino, String titulo, DefaultTableModel datos) {

        if (destino == null) {
            return Resultado.error("Indique donde guardar el reporte");
        }

        String error = GeneradorPDF.generarReporte(destino, titulo, datos);

        return error.isEmpty()
                ? Resultado.exito("Reporte exportado a PDF:\n" + destino.getAbsolutePath())
                : Resultado.error(error);
    }

    /** Exporta un reporte de pantalla a un archivo que Excel abre directamente (RF-24). */
    public Resultado exportarExcel(File destino, DefaultTableModel datos) {

        if (destino == null) {
            return Resultado.error("Indique donde guardar el reporte");
        }

        String error = ExportadorCSV.exportar(destino, datos);

        return error.isEmpty()
                ? Resultado.exito("Reporte exportado para Excel:\n" + destino.getAbsolutePath())
                : Resultado.error(error);
    }
}
