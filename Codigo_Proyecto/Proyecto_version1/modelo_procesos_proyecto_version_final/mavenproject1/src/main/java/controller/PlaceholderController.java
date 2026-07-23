package controller;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

public class PlaceholderController {

    private static final Color COLOR_MARCADOR = new Color(153, 153, 153);
    private static final Color COLOR_TEXTO_ACTIVO = Color.BLACK;

    public void aplicarACampo(JTextField campo, String textoGuia) {
        campo.setText(textoGuia);
        campo.setForeground(COLOR_MARCADOR);

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (campo.getText().equals(textoGuia)) {
                    campo.setText("");
                    campo.setForeground(COLOR_TEXTO_ACTIVO);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.setText(textoGuia);
                    campo.setForeground(COLOR_MARCADOR);
                }
            }
        });
    }


    public void aplicarACampoPassword(JPasswordField campo, String textoGuia) {
        campo.setText(textoGuia);
        campo.setForeground(COLOR_MARCADOR);

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String textoActual = String.valueOf(campo.getPassword());
                if (textoActual.equals(textoGuia)) {
                    campo.setText("");
                    campo.setForeground(COLOR_TEXTO_ACTIVO);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String textoActual = String.valueOf(campo.getPassword());
                if (textoActual.isEmpty()) {
                    campo.setText(textoGuia);
                    campo.setForeground(COLOR_MARCADOR);
                }
            }
        });
    }
}
