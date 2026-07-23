package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Paint;
import javafx.util.StringConverter;

/**
 * Gráficos de barras construidos con JavaFX (embebidos en Swing vía
 * {@link JFXPanel}), usados en la pestaña de reporte del dashboard.
 */
public final class GraficoFX {

    private static final int ALTURA_POR_BARRA = 32;
    private static final int ALTURA_MINIMA    = 60;
    private static final int ALTURA_MENSUAL   = 260;

    private static final Paint COLOR_TEXTO = javafx.scene.paint.Color.rgb(
        EstiloUI.COLOR_MUY_OSCURO.getRed(),
        EstiloUI.COLOR_MUY_OSCURO.getGreen(),
        EstiloUI.COLOR_MUY_OSCURO.getBlue());

    private GraficoFX() {}

    /**
     * Crea un panel Swing con un gráfico de barras horizontal en JavaFX, para
     * comparar el valor de una sola métrica entre etiquetas de una categoría.
     *
     * @param datos      pares etiqueta-valor, en el orden en que deben mostrarse.
     * @param colorBarra color de relleno de las barras.
     * @return {@link JFXPanel} listo para insertarse en un layout Swing.
     */
    public static JFXPanel crearBarrasHorizontales(Map<String, Integer> datos, Color colorBarra) {
        JFXPanel panelFx = new JFXPanel();
        int altura = Math.max(ALTURA_MINIMA, datos.size() * ALTURA_POR_BARRA + 20);
        panelFx.setPreferredSize(new Dimension(10, altura));
        panelFx.setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
        panelFx.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panelFx.setOpaque(false);

        String hoja = hojaEstilo(hex(colorBarra));

        Platform.runLater(() -> {
            NumberAxis ejeValor = new NumberAxis();
            ejeValor.setMinorTickVisible(false);
            ejeValor.setTickLabelFill(COLOR_TEXTO);

            CategoryAxis ejeCategoria = new CategoryAxis();
            ejeCategoria.setTickLabelFill(COLOR_TEXTO);
            ejeCategoria.getCategories().addAll(datos.keySet());

            BarChart<Number, String> grafico = new BarChart<>(ejeValor, ejeCategoria);
            grafico.setLegendVisible(false);
            grafico.setAnimated(true);
            grafico.setCategoryGap(10);
            grafico.setBarGap(0);
            grafico.setStyle("-fx-background-color: transparent;");

            XYChart.Series<Number, String> serie = new XYChart.Series<>();
            datos.forEach((etiqueta, valor) -> serie.getData().add(new XYChart.Data<>(valor, etiqueta)));
            grafico.getData().add(serie);

            Scene escena = new Scene(grafico);
            escena.setFill(javafx.scene.paint.Color.TRANSPARENT);
            escena.getStylesheets().add(hoja);
            panelFx.setScene(escena);
        });

        return panelFx;
    }

    /**
     * Crea un panel Swing con un gráfico de barras vertical agrupado en JavaFX
     * (estilo reporte contable), comparando Pedidas/Compradas/Canceladas mes a mes.
     *
     * @param meses        etiquetas de mes en orden cronológico (eje X).
     * @param pedidas      valores de "pedidas" por mes (mismas claves que {@code meses}).
     * @param compradas    valores de "compradas" por mes.
     * @param canceladas   valores de "canceladas" por mes.
     * @param formatoMoneda si es {@code true}, el eje Y se muestra con formato "$".
     * @return {@link JFXPanel} listo para insertarse en un layout Swing.
     */
    public static JFXPanel crearBarrasMensuales(
            List<String> meses,
            Map<String, Number> pedidas,
            Map<String, Number> compradas,
            Map<String, Number> canceladas,
            boolean formatoMoneda) {

        JFXPanel panelFx = new JFXPanel();
        panelFx.setPreferredSize(new Dimension(10, ALTURA_MENSUAL));
        panelFx.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTURA_MENSUAL));
        panelFx.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panelFx.setOpaque(false);

        // El orden debe coincidir con el orden en que se agregan las series abajo
        // (series0 = Pedidas, series1 = Compradas, series2 = Canceladas), ya que
        // JavaFX asigna la clase .default-colorN tanto a las barras como a su
        // símbolo en la leyenda según ese orden.
        String hoja = hojaEstilo(
            hex(EstiloUI.COLOR_PRIMARIO),
            hex(EstiloUI.color("639922")),
            hex(EstiloUI.color("791F1F")));

        Platform.runLater(() -> {
            CategoryAxis ejeMes = new CategoryAxis();
            ejeMes.getCategories().addAll(meses);
            ejeMes.setTickLabelFill(COLOR_TEXTO);

            NumberAxis ejeValor = new NumberAxis();
            ejeValor.setTickLabelFill(COLOR_TEXTO);
            if (formatoMoneda) {
                ejeValor.setTickLabelFormatter(new StringConverter<Number>() {
                    @Override public String toString(Number valor) { return "$" + valor.intValue(); }
                    @Override public Number fromString(String cadena) { return 0; }
                });
            }

            BarChart<String, Number> grafico = new BarChart<>(ejeMes, ejeValor);
            grafico.setAnimated(true);
            grafico.setLegendVisible(true);
            grafico.setCategoryGap(16);
            grafico.setBarGap(2);
            grafico.setStyle("-fx-background-color: transparent;");

            grafico.getData().addAll(
                serieDesde("Pedidas",    meses, pedidas),
                serieDesde("Compradas",  meses, compradas),
                serieDesde("Canceladas", meses, canceladas));

            Scene escena = new Scene(grafico);
            escena.setFill(javafx.scene.paint.Color.TRANSPARENT);
            escena.getStylesheets().add(hoja);
            panelFx.setScene(escena);
        });

        return panelFx;
    }

    private static <X> XYChart.Series<X, Number> serieDesde(String nombre, List<X> claves, Map<X, Number> datos) {
        XYChart.Series<X, Number> serie = new XYChart.Series<>();
        serie.setName(nombre);
        for (X clave : claves) {
            serie.getData().add(new XYChart.Data<>(clave, datos.getOrDefault(clave, 0)));
        }
        return serie;
    }

    /**
     * Construye una hoja de estilos en línea (data URI en base64, para evitar
     * problemas de escape con caracteres como {@code #}) que fuerza el mismo
     * color en las barras y en su símbolo de leyenda correspondiente, según el
     * índice de cada serie ({@code .default-color0}, {@code .default-color1}...).
     */
    private static String hojaEstilo(String... coloresHex) {
        StringBuilder css = new StringBuilder();
        for (int i = 0; i < coloresHex.length; i++) {
            css.append(".default-color").append(i).append(".chart-bar { -fx-bar-fill: ").append(coloresHex[i]).append("; }");
            css.append(".default-color").append(i).append(".chart-legend-item-symbol { -fx-background-color: ").append(coloresHex[i]).append("; }");
        }
        String base64 = Base64.getEncoder().encodeToString(css.toString().getBytes(StandardCharsets.UTF_8));
        return "data:text/css;base64," + base64;
    }

    private static String hex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
