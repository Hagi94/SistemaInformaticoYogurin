package util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

/**
 * Clase de utilidad para generar documentos PDF (RF-16, RF-24).
 * Usa la libreria OpenPDF, incluida en la carpeta lib del proyecto.
 */
public class GeneradorPDF {

    private static final String EMPRESA = "YOGURIN BUSTAMANTE";
    private static final String RUBRO = "Elaboracion y venta de yogurt artesanal";
    private static final String DIRECCION = "Sayan - Huaura - Lima";

    private static final Color COLOR_CABECERA = new Color(158, 43, 78);   // (luiggi) color de la marca
    private static final Color COLOR_FILA_PAR = new Color(246, 233, 237);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font F_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font F_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font F_CABECERA_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font F_PIE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY);

    private GeneradorPDF() {                                 // (luiggi) clase de utilidad, no se instancia
    }

    /**
     * Genera el comprobante de una venta para entregarlo al cliente (RF-16).
     *
     * @param cabecera {numero, fecha, cliente, dni, total, descuento, totalPagar}
     * @return mensaje vacio si todo salio bien, o la descripcion del error.
     */
    public static String generarComprobante(File destino, String[] cabecera, DefaultTableModel detalle) {

        if (cabecera == null || cabecera.length < 7) {
            return "No se pudo leer la venta";               // (luiggi) valida antes de crear el archivo
        }

        Document doc = new Document(PageSize.A5, 32, 32, 28, 28); // (luiggi) A5: tamano de boleta

        try (FileOutputStream salida = new FileOutputStream(destino)) {

            PdfWriter.getInstance(doc, salida);
            doc.open();

            escribirEncabezado(doc, "COMPROBANTE DE VENTA");

            doc.add(new Paragraph("N " + cabecera[0], F_SECCION));
            doc.add(new Paragraph("Fecha: " + cabecera[1], F_NORMAL));
            doc.add(new Paragraph("Cliente: " + cabecera[2], F_NORMAL));
            doc.add(new Paragraph("DNI: " + cabecera[3], F_NORMAL));
            doc.add(espacio(10));

            doc.add(construirTabla(detalle, new float[]{4.2f, 1.3f, 1.7f, 1.7f}));
            doc.add(espacio(8));

            doc.add(alineadoDerecha("Total:  S/ " + cabecera[4], F_NORMAL));
            doc.add(alineadoDerecha("Descuento:  S/ " + cabecera[5], F_NORMAL));
            doc.add(alineadoDerecha("TOTAL A PAGAR:  S/ " + cabecera[6], F_SECCION));

            doc.add(espacio(14));
            doc.add(centrado("Gracias por su compra", F_PIE));
            doc.add(centrado("Documento interno, no valido como comprobante tributario", F_PIE));

            doc.close();
            return "";                                       // (luiggi) cadena vacia = exito

        } catch (DocumentException | IOException e) {
            return "Error al generar el comprobante: " + e.getMessage();
        }
    }

    /**
     * Exporta cualquier reporte de pantalla a un PDF horizontal (RF-24).
     *
     * @return mensaje vacio si todo salio bien, o la descripcion del error.
     */
    public static String generarReporte(File destino, String titulo, DefaultTableModel datos) {

        if (datos == null || datos.getRowCount() == 0) {
            return "El reporte no tiene datos para exportar"; // (luiggi) evita generar un PDF vacio
        }

        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36); // (luiggi) horizontal: caben mas columnas

        try (FileOutputStream salida = new FileOutputStream(destino)) {

            PdfWriter.getInstance(doc, salida);
            doc.open();

            escribirEncabezado(doc, titulo.toUpperCase());

            doc.add(new Paragraph("Registros: " + datos.getRowCount(), F_NORMAL));
            doc.add(espacio(10));

            doc.add(construirTabla(datos, null));            // (luiggi) columnas de ancho automatico

            doc.add(espacio(12));
            doc.add(centrado("Generado por el Sistema Informatico de Gestion - " + fechaHora(), F_PIE));

            doc.close();
            return "";

        } catch (DocumentException | IOException e) {
            return "Error al exportar el reporte: " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------
    // Metodos privados de apoyo
    // ------------------------------------------------------------------

    /** Escribe el membrete de la empresa comun a todos los documentos. */
    private static void escribirEncabezado(Document doc, String titulo) throws DocumentException {

        doc.add(centrado(EMPRESA, F_TITULO));
        doc.add(centrado(RUBRO, F_SUBTITULO));
        doc.add(centrado(DIRECCION, F_SUBTITULO));
        doc.add(espacio(10));
        doc.add(centrado(titulo, F_SECCION));
        doc.add(espacio(8));
    }

    /** Construye la tabla del PDF a partir del modelo que muestra la pantalla. */
    private static PdfPTable construirTabla(DefaultTableModel datos, float[] anchos)
            throws DocumentException {

        int columnas = datos.getColumnCount();
        PdfPTable tabla = new PdfPTable(columnas);
        tabla.setWidthPercentage(100);

        if (anchos != null && anchos.length == columnas) {
            tabla.setWidths(anchos);
        }

        for (int c = 0; c < columnas; c++) {
            PdfPCell celda = new PdfPCell(new Phrase(datos.getColumnName(c), F_CABECERA_TABLA));
            celda.setBackgroundColor(COLOR_CABECERA);        // (luiggi) cabecera con el color de la marca
            celda.setPadding(5);
            celda.setBorderColor(COLOR_CABECERA);
            tabla.addCell(celda);
        }

        for (int f = 0; f < datos.getRowCount(); f++) {
            for (int c = 0; c < columnas; c++) {

                Object valor = datos.getValueAt(f, c);
                PdfPCell celda = new PdfPCell(
                        new Phrase(valor == null ? "" : valor.toString(), F_NORMAL));

                celda.setPadding(4);
                celda.setBorderColor(Color.LIGHT_GRAY);

                if (f % 2 == 1) {
                    celda.setBackgroundColor(COLOR_FILA_PAR); // (luiggi) filas alternas, mas facil de leer
                }
                if (c > 0) {
                    celda.setHorizontalAlignment(Element.ALIGN_RIGHT); // (luiggi) los numeros alineados a la derecha
                }
                tabla.addCell(celda);
            }
        }
        return tabla;
    }

    private static Paragraph centrado(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private static Paragraph alineadoDerecha(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private static Paragraph espacio(float altura) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(altura);
        return p;
    }

    private static String fechaHora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
