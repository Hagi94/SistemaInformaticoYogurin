package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.swing.table.DefaultTableModel;

/**
 * Clase de utilidad para exportar reportes a un archivo que Excel abre directamente (RF-24).
 * Genera CSV con separador de punto y coma y marca BOM, que es el formato que espera
 * Excel en configuracion regional de espanol.
 */
public class ExportadorCSV {

    private static final char SEPARADOR = ';';   // (luiggi) Excel en espanol usa punto y coma, no coma
    private static final String BOM = "﻿";  // (luiggi) sin BOM, Excel muestra mal las tildes

    private ExportadorCSV() {                    // (luiggi) clase de utilidad, no se instancia
    }

    /**
     * Escribe el contenido de la tabla en un archivo CSV.
     *
     * @return mensaje vacio si todo salio bien, o la descripcion del error.
     */
    public static String exportar(File destino, DefaultTableModel datos) {

        if (datos == null || datos.getRowCount() == 0) {
            return "El reporte no tiene datos para exportar"; // (luiggi) evita generar un archivo vacio
        }

        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(destino), StandardCharsets.UTF_8)) {

            w.write(BOM);

            for (int c = 0; c < datos.getColumnCount(); c++) {
                if (c > 0) {
                    w.write(SEPARADOR);
                }
                w.write(escapar(datos.getColumnName(c)));
            }
            w.write("\r\n");                                 // (luiggi) salto de linea de Windows

            for (int f = 0; f < datos.getRowCount(); f++) {
                for (int c = 0; c < datos.getColumnCount(); c++) {
                    if (c > 0) {
                        w.write(SEPARADOR);
                    }
                    Object valor = datos.getValueAt(f, c);
                    w.write(escapar(valor == null ? "" : valor.toString()));
                }
                w.write("\r\n");
            }

            return "";                                       // (luiggi) cadena vacia = exito

        } catch (IOException e) {
            return "Error al exportar a Excel: " + e.getMessage();
        }
    }

    /**
     * Encierra el valor entre comillas si contiene el separador, comillas o saltos de linea.
     * Sin esto, un campo como "Lima, Peru" partiria la fila en dos columnas.
     */
    private static String escapar(String valor) {

        boolean necesitaComillas = valor.indexOf(SEPARADOR) >= 0
                || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0
                || valor.indexOf('\r') >= 0;

        if (!necesitaComillas) {
            return valor;
        }
        return '"' + valor.replace("\"", "\"\"") + '"';      // (luiggi) las comillas internas se duplican
    }
}
