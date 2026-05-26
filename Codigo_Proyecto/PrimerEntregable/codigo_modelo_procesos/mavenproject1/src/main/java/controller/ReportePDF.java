package controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import model.Pedido;
import model.Producto;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Genera reportes en PDF de los pedidos del sistema.
 *
 * Uso:
 *   ReportePDF.generarReportePedidos(listaPedidos, "Reporte General", componentePadre);
 *   ReportePDF.generarTicketPedido(pedido, componentePadre);
 */
public class ReportePDF {

    // ── Colores de la app ────────────────────────────────────────────────────────
    private static final Color ROSA         = new Color(255, 102, 153);
    private static final Color ROSA_CLARO   = new Color(255, 220, 235);
    private static final Color GRIS_CLARO   = new Color(245, 245, 245);
    private static final Color GRIS_BORDE   = new Color(220, 220, 220);
    private static final Color TEXTO        = new Color(30, 30, 30);

    // ── Colores por estado ───────────────────────────────────────────────────────
    private static final Color FONDO_PENDIENTE   = new Color(250, 238, 218);
    private static final Color BORDE_PENDIENTE   = new Color(239, 159, 39);
    private static final Color FONDO_COBRADO     = new Color(234, 243, 222);
    private static final Color BORDE_COBRADO     = new Color(99,  153, 34);
    private static final Color FONDO_CANCELADO   = new Color(241, 239, 232);
    private static final Color BORDE_CANCELADO   = new Color(136, 135, 128);

    // ── Fuentes ──────────────────────────────────────────────────────────────────
    private static final Font F_HEADER_TITULO  = new Font(Font.HELVETICA, 22, Font.BOLD,   Color.WHITE);
    private static final Font F_HEADER_SUB     = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.WHITE);
    private static final Font F_HEADER_FECHA   = new Font(Font.HELVETICA,  9, Font.ITALIC, ROSA_CLARO);
    private static final Font F_SECCION        = new Font(Font.HELVETICA, 11, Font.BOLD,   ROSA);
    private static final Font F_STAT_VALOR     = new Font(Font.HELVETICA, 15, Font.BOLD,   ROSA);
    private static final Font F_STAT_ETIQUETA  = new Font(Font.HELVETICA,  8, Font.NORMAL, Color.GRAY);
    private static final Font F_CABECERA_TABLA = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.WHITE);
    private static final Font F_DATO_NEGRITA   = new Font(Font.HELVETICA,  9, Font.BOLD,   TEXTO);
    private static final Font F_DATO_NORMAL    = new Font(Font.HELVETICA,  9, Font.NORMAL, TEXTO);
    private static final Font F_DATO_GRIS      = new Font(Font.HELVETICA,  8, Font.ITALIC, Color.GRAY);
    private static final Font F_PIE            = new Font(Font.HELVETICA,  8, Font.NORMAL, Color.GRAY);

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DIA_LARGO  =
        DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    // ─────────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Genera un reporte completo con todos los pedidos recibidos.
     *
     * @param pedidos  lista de pedidos a incluir en el reporte
     * @param titulo   titulo del reporte (ej: "Reporte General", "Pedidos Pendientes")
     * @param padre    componente padre para centrar los dialogos (puede ser null)
     */
    public static void generarReportePedidos(List<Pedido> pedidos, String titulo, java.awt.Component padre) {
        File archivo = elegirArchivoDestino(padre, "reporte_pedidos.pdf");
        if (archivo == null) return;

        try {
            construirReportePedidos(pedidos, titulo, archivo);
            ofrecerAbrir(archivo, padre);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(padre,
                "Error al generar el PDF:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Genera un ticket de un solo pedido con su detalle de productos.
     *
     * @param pedido  pedido a imprimir
     * @param padre   componente padre para los dialogos
     */
    public static void generarTicketPedido(Pedido pedido, java.awt.Component padre) {
        String nombreArchivo = "ticket_" + pedido.getCliente().getNombre()
            .replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        File archivo = elegirArchivoDestino(padre, nombreArchivo);
        if (archivo == null) return;

        try {
            construirTicket(pedido, archivo);
            ofrecerAbrir(archivo, padre);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(padre,
                "Error al generar el ticket:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Construccion del reporte de pedidos
    // ─────────────────────────────────────────────────────────────────────────────

    private static void construirReportePedidos(List<Pedido> pedidos, String titulo, File archivo)
            throws Exception {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        writer.setPageEvent(eventoPieDePagina());
        doc.open();

        doc.add(crearEncabezado(titulo));
        doc.add(espaciado(6));
        doc.add(crearResumen(pedidos));
        doc.add(espaciado(8));

        if (pedidos.isEmpty()) {
            Paragraph sinDatos = new Paragraph("No hay pedidos para mostrar.", F_DATO_GRIS);
            sinDatos.setAlignment(Element.ALIGN_CENTER);
            doc.add(sinDatos);
        } else {
            Paragraph secPedidos = new Paragraph("DETALLE DE PEDIDOS", F_SECCION);
            secPedidos.setSpacingAfter(8);
            doc.add(secPedidos);

            for (Pedido pedido : pedidos) {
                doc.add(crearBloquesPedido(pedido));
                doc.add(espaciado(6));
            }
        }

        doc.close();
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Construccion del ticket individual
    // ─────────────────────────────────────────────────────────────────────────────

    private static void construirTicket(Pedido pedido, File archivo) throws Exception {
        Document doc = new Document(PageSize.A5, 28, 28, 28, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        writer.setPageEvent(eventoPieDePagina());
        doc.open();

        // Encabezado compacto
        doc.add(crearEncabezado("Ticket de pedido"));
        doc.add(espaciado(6));

        // Info del cliente
        Paragraph infoCliente = new Paragraph();
        infoCliente.add(new Chunk("Cliente: ", F_DATO_NEGRITA));
        infoCliente.add(new Chunk(pedido.getCliente().getNombre(), F_DATO_NORMAL));
        infoCliente.setSpacingAfter(2);
        doc.add(infoCliente);

        Paragraph infoEstado = new Paragraph();
        infoEstado.add(new Chunk("Estado: ", F_DATO_NEGRITA));
        infoEstado.add(new Chunk(pedido.getEstado(), F_DATO_NORMAL));
        infoEstado.setSpacingAfter(2);
        doc.add(infoEstado);

        if (pedido.getFechaRegistro() != null) {
            Paragraph infoFecha = new Paragraph();
            infoFecha.add(new Chunk("Fecha: ", F_DATO_NEGRITA));
            infoFecha.add(new Chunk(pedido.getFechaRegistro().format(FMT_FECHA_HORA), F_DATO_NORMAL));
            infoFecha.setSpacingAfter(8);
            doc.add(infoFecha);
        }

        // Tabla de productos
        if (!pedido.getProductos().isEmpty()) {
            doc.add(crearTablaProductos(pedido.getProductos()));
        }

        // Total
        doc.add(espaciado(6));
        PdfPTable tablaTotal = new PdfPTable(new float[]{3f, 1f});
        tablaTotal.setWidthPercentage(100);
        PdfPCell cEtTotal = new PdfPCell(new Phrase("TOTAL", F_CABECERA_TABLA));
        cEtTotal.setBackgroundColor(ROSA);
        cEtTotal.setPadding(6);
        cEtTotal.setBorder(Rectangle.NO_BORDER);
        PdfPCell cValTotal = new PdfPCell(new Phrase(
            "$" + pedido.getTotal().toPlainString(),
            new Font(Font.HELVETICA, 12, Font.BOLD, TEXTO)));
        cValTotal.setPadding(6);
        cValTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cValTotal.setBorderColor(GRIS_BORDE);
        tablaTotal.addCell(cEtTotal);
        tablaTotal.addCell(cValTotal);
        doc.add(tablaTotal);

        doc.close();
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Componentes visuales del PDF
    // ─────────────────────────────────────────────────────────────────────────────

    /** Banda superior rosa con nombre de la app y titulo del reporte. */
    private static PdfPTable crearEncabezado(String titulo) throws DocumentException {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(0);

        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(ROSA);
        celda.setPadding(14);
        celda.setBorder(Rectangle.NO_BORDER);

        Paragraph pNombre = new Paragraph("PinkyPuff", F_HEADER_TITULO);
        pNombre.setAlignment(Element.ALIGN_LEFT);
        celda.addElement(pNombre);

        Paragraph pTitulo = new Paragraph(titulo, F_HEADER_SUB);
        pTitulo.setSpacingBefore(2);
        celda.addElement(pTitulo);

        Paragraph pFecha = new Paragraph(
            "Generado el " + LocalDate.now().format(FMT_DIA_LARGO), F_HEADER_FECHA);
        pFecha.setSpacingBefore(4);
        celda.addElement(pFecha);

        tabla.addCell(celda);
        return tabla;
    }


    /** Fila de cajas con estadisticas: total, pendientes, cobrados, etc. */
    private static PdfPTable crearResumen(List<Pedido> pedidos) throws DocumentException {
        long pendientes = pedidos.stream().filter(p -> "PENDIENTE".equals(p.getEstado())).count();
        long cobrados   = pedidos.stream().filter(p -> "COBRADO".equals(p.getEstado())).count();
        long cancelados = pedidos.stream().filter(p -> "CANCELADO".equals(p.getEstado())).count();
        BigDecimal totalCobrado = pedidos.stream()
            .filter(p -> "COBRADO".equals(p.getEstado()))
            .map(Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal porCobrar = pedidos.stream()
            .filter(p -> "PENDIENTE".equals(p.getEstado()))
            .map(Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Paragraph seccion = new Paragraph("RESUMEN", F_SECCION);
        seccion.setSpacingAfter(6);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1f, 1f, 1f, 1f, 1.4f});

        celdaStat(tabla, String.valueOf(pedidos.size()),               "Total pedidos");
        celdaStat(tabla, String.valueOf(pendientes),                   "Pendientes");
        celdaStat(tabla, String.valueOf(cobrados),                     "Cobrados");
        celdaStat(tabla, String.valueOf(cancelados),                   "Cancelados");
        celdaStat(tabla, "$" + totalCobrado.toPlainString(),          "Total cobrado");

        // Segunda fila: por cobrar ocupa toda la fila
        PdfPCell celdaPorCobrar = new PdfPCell();
        celdaPorCobrar.setColspan(5);
        celdaPorCobrar.setBackgroundColor(FONDO_PENDIENTE);
        celdaPorCobrar.setBorderColor(BORDE_PENDIENTE);
        celdaPorCobrar.setPadding(6);
        Paragraph pPorCobrar = new Paragraph();
        pPorCobrar.add(new Chunk("Por cobrar (pendientes): ", F_DATO_NEGRITA));
        pPorCobrar.add(new Chunk("$" + porCobrar.toPlainString(),
            new Font(Font.HELVETICA, 10, Font.BOLD, BORDE_PENDIENTE)));
        celdaPorCobrar.addElement(pPorCobrar);
        tabla.addCell(celdaPorCobrar);

        // Envuelvo seccion + tabla en un fragmento
        PdfPTable envoltorio = new PdfPTable(1);
        envoltorio.setWidthPercentage(100);
        envoltorio.setSpacingAfter(4);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        c.addElement(seccion);
        c.addElement(tabla);
        envoltorio.addCell(c);
        return envoltorio;
    }


    /** Bloque de un pedido: encabezado + tabla de productos. */
    private static PdfPTable crearBloquesPedido(Pedido pedido) throws DocumentException {
        Color colorFondo = colorFondo(pedido.getEstado());
        Color colorBorde = colorBorde(pedido.getEstado());

        PdfPTable tarjeta = new PdfPTable(1);
        tarjeta.setWidthPercentage(100);
        tarjeta.setKeepTogether(true);

        PdfPCell contenedor = new PdfPCell();
        contenedor.setBorderColor(colorBorde);
        contenedor.setBorderWidth(1.5f);
        contenedor.setPadding(0);

        // ─ Fila de datos del pedido
        PdfPTable filaDatos = new PdfPTable(new float[]{3f, 1.2f, 2f, 1.2f});
        filaDatos.setWidthPercentage(100);

        celdaEtiqueta(filaDatos, "CLIENTE",  colorFondo);
        celdaEtiqueta(filaDatos, "ESTADO",   colorFondo);
        celdaEtiqueta(filaDatos, "FECHA",    colorFondo);
        celdaEtiqueta(filaDatos, "TOTAL",    colorFondo);

        celdaDato(filaDatos, pedido.getCliente().getNombre(), colorFondo);
        celdaDato(filaDatos, pedido.getEstado(),              colorFondo);
        celdaDato(filaDatos,
            pedido.getFechaRegistro() != null
                ? pedido.getFechaRegistro().format(FMT_FECHA_HORA) : "—",
            colorFondo);
        celdaDato(filaDatos, "$" + pedido.getTotal().toPlainString(), colorFondo);

        contenedor.addElement(filaDatos);

        // ─ Tabla de productos (si hay)
        if (!pedido.getProductos().isEmpty()) {
            contenedor.addElement(crearTablaProductos(pedido.getProductos()));
        }

        tarjeta.addCell(contenedor);
        return tarjeta;
    }


    /** Tabla de productos con cabecera rosa y filas alternadas. */
    private static PdfPTable crearTablaProductos(List<Producto> productos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{1.3f, 1.3f, 0.8f, 2f, 0.5f, 1f});
        tabla.setWidthPercentage(100);

        // Cabecera
        String[] cols = {"Tipo", "Estilo", "Talla", "Descripcion / Extras", "Cant.", "Precio"};
        for (String col : cols) {
            PdfPCell c = new PdfPCell(new Phrase(col, F_CABECERA_TABLA));
            c.setBackgroundColor(ROSA);
            c.setPadding(5);
            c.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(c);
        }

        // Filas
        boolean par = false;
        for (Producto prod : productos) {
            Color bg = par ? GRIS_CLARO : Color.WHITE;
            par = !par;

            // Descripcion + atributos extra juntos
            StringBuilder extras = new StringBuilder();
            if (prod.getAtributos() != null) {
                for (Map.Entry<String, String> attr : prod.getAtributos().entrySet()) {
                    if (attr.getValue() != null && !attr.getValue().isBlank()) {
                        if (extras.length() > 0) extras.append(" | ");
                        extras.append(attr.getKey()).append(": ").append(attr.getValue());
                    }
                }
            }
            String descripcion = vacio(prod.getDescripcion())
                ? (extras.length() > 0 ? extras.toString() : "—")
                : prod.getDescripcion() + (extras.length() > 0 ? "  (" + extras + ")" : "");

            BigDecimal subtotal = prod.getPrecio().multiply(BigDecimal.valueOf(prod.getCantidad()));

            celdaProducto(tabla, nvl(prod.getTipo()),           bg);
            celdaProducto(tabla, nvl(prod.getEstilo()),         bg);
            celdaProducto(tabla, nvl(prod.getTalla()),          bg);
            celdaProducto(tabla, descripcion,                   bg);
            celdaProducto(tabla, String.valueOf(prod.getCantidad()), bg);
            celdaProducto(tabla, "$" + subtotal.toPlainString(), bg);
        }

        return tabla;
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Celdas auxiliares
    // ─────────────────────────────────────────────────────────────────────────────

    private static void celdaStat(PdfPTable tabla, String valor, String etiqueta) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(GRIS_CLARO);
        c.setBorderColor(GRIS_BORDE);
        c.setPadding(8);
        Paragraph pv = new Paragraph(valor, F_STAT_VALOR);
        pv.setAlignment(Element.ALIGN_CENTER);
        Paragraph pe = new Paragraph(etiqueta, F_STAT_ETIQUETA);
        pe.setAlignment(Element.ALIGN_CENTER);
        c.addElement(pv);
        c.addElement(pe);
        tabla.addCell(c);
    }


    private static void celdaEtiqueta(PdfPTable tabla, String texto, Color fondo) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_DATO_GRIS));
        c.setBackgroundColor(fondo);
        c.setPadding(4);
        c.setBorderColor(GRIS_BORDE);
        tabla.addCell(c);
    }


    private static void celdaDato(PdfPTable tabla, String texto, Color fondo) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_DATO_NEGRITA));
        c.setBackgroundColor(fondo);
        c.setPadding(5);
        c.setBorderColor(GRIS_BORDE);
        tabla.addCell(c);
    }


    private static void celdaProducto(PdfPTable tabla, String texto, Color fondo) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_DATO_NORMAL));
        c.setBackgroundColor(fondo);
        c.setPadding(4);
        c.setBorderColor(new Color(235, 235, 235));
        tabla.addCell(c);
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────────────────────

    private static PdfPageEventHelper eventoPieDePagina() {
        return new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                Phrase pie = new Phrase(
                    "PinkyPuff  ·  Pagina " + w.getPageNumber(), F_PIE);
                ColumnText.showTextAligned(
                    w.getDirectContent(), Element.ALIGN_CENTER, pie,
                    (d.right() - d.left()) / 2 + d.leftMargin(),
                    d.bottom() - 15, 0);
            }
        };
    }


    private static Paragraph espaciado(float puntos) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(puntos);
        return p;
    }


    /** Abre un JFileChooser para que el usuario elija donde guardar el PDF. */
    private static File elegirArchivoDestino(java.awt.Component padre, String nombreSugerido) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar PDF");
        selector.setSelectedFile(new File(nombreSugerido));
        selector.setFileFilter(new FileNameExtensionFilter("Archivo PDF (*.pdf)", "pdf"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return null;
        File f = selector.getSelectedFile();
        return f.getName().toLowerCase().endsWith(".pdf") ? f : new File(f.getAbsolutePath() + ".pdf");
    }


    private static void ofrecerAbrir(File archivo, java.awt.Component padre) {
        int r = JOptionPane.showConfirmDialog(padre,
            "PDF generado correctamente.\n¿Deseas abrirlo ahora?",
            "Listo", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            try { java.awt.Desktop.getDesktop().open(archivo); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(padre,
                    "No se pudo abrir el archivo automaticamente.\nRuta: " + archivo.getAbsolutePath(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


    private static Color colorFondo(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> FONDO_PENDIENTE;
            case "COBRADO"   -> FONDO_COBRADO;
            default          -> FONDO_CANCELADO;
        };
    }


    private static Color colorBorde(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> BORDE_PENDIENTE;
            case "COBRADO"   -> BORDE_COBRADO;
            default          -> BORDE_CANCELADO;
        };
    }


    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }


    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
