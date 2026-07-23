package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javax.swing.SwingUtilities;

/**
 * Barra de navegación superior construida con JavaFX (embebida en Swing vía
 * {@link JFXPanel}), usada como la parte visual de la navegación principal
 * del dashboard.
 *
 * <p>El cambio real de contenido lo sigue manejando un {@link java.awt.CardLayout}
 * en Swing; esta barra solo se encarga de la apariencia de las pestañas y de
 * notificar (en el hilo de Swing) cuál fue seleccionada.</p>
 */
public class BarraNavegacionFX {

    private static final int ALTURA = 44;

    private final JFXPanel panelFx = new JFXPanel();
    private final List<Label> pestanas = new ArrayList<>();
    private int indiceActivo = 0;

    public BarraNavegacionFX(List<String> titulos, IntConsumer alSeleccionar) {
        panelFx.setPreferredSize(new Dimension(10, ALTURA));
        panelFx.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTURA));

        Platform.runLater(() -> {
            HBox barra = new HBox(4);
            barra.setPadding(new Insets(6, 10, 6, 10));
            barra.setStyle("-fx-background-color: rgb(255, 255, 255);");

            for (int i = 0; i < titulos.size(); i++) {
                int indice = i;
                Label pestana = new Label(titulos.get(i));
                pestana.getStyleClass().add("tab-pestania");
                if (indice == indiceActivo) pestana.getStyleClass().add("tab-pestania-activa");
                pestana.setOnMouseClicked(e -> seleccionar(indice, alSeleccionar));
                pestanas.add(pestana);
                barra.getChildren().add(pestana);
            }

            Scene escena = new Scene(barra);
            barra.prefWidthProperty().bind(escena.widthProperty());
            escena.setFill(javafx.scene.paint.Color.TRANSPARENT);
            escena.getStylesheets().add(hojaEstilo());
            panelFx.setScene(escena);
        });
    }

    /** Panel Swing listo para insertarse en un layout (ej. {@code BorderLayout.NORTH}). */
    public JFXPanel getPanel() {
        return panelFx;
    }

    private void seleccionar(int indice, IntConsumer alSeleccionar) {
        indiceActivo = indice;
        for (int i = 0; i < pestanas.size(); i++) {
            Label pestana = pestanas.get(i);
            pestana.getStyleClass().remove("tab-pestania-activa");
            if (i == indice) pestana.getStyleClass().add("tab-pestania-activa");
        }
        // El callback toca Swing (CardLayout): debe correr en el EDT, no en el hilo de JavaFX.
        SwingUtilities.invokeLater(() -> alSeleccionar.accept(indice));
    }

    private static String hojaEstilo() {
        String css =
            ".tab-pestania {" +
                "-fx-text-fill: rgb(0, 0, 0);" +
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16 8 16;" +
                "-fx-font-size: 13px;" +
                "-fx-cursor: hand;" +
            "}" +
            ".tab-pestania:hover {" +
                "-fx-background-color: " + hex(oscurecer(EstiloUI.COLOR_PRIMARIO, 0.15)) + ";" +
            "}" +
            ".tab-pestania-activa {" +
                "-fx-background-color: " + hex(EstiloUI.COLOR_PRIMARIO) + ";" +
                "-fx-text-fill: " + hex(EstiloUI.COLOR_MUY_OSCURO) + ";" +
                "-fx-font-weight: bold;" +
            "}" +
            ".tab-pestania-activa:hover {" +
                "-fx-background-color: " + hex(EstiloUI.COLOR_PRIMARIO) + ";" +
            "}";
        String base64 = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        return "data:text/css;base64," + base64;
    }

    private static String hex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Oscurece un color un {@code factor} (0-1) manteniendo su tono. */
    private static Color oscurecer(Color color, double factor) {
        int r = (int) Math.round(color.getRed()   * (1 - factor));
        int g = (int) Math.round(color.getGreen() * (1 - factor));
        int b = (int) Math.round(color.getBlue()  * (1 - factor));
        return new Color(r, g, b);
    }
}
