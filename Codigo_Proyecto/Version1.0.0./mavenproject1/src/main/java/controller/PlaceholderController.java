
package controller;

/**
 *
 * @author yesteb
 */
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

public class PlaceholderController {

    private static final Color PLACEHOLDER_COLOR = new Color(153,153,153);
    private static final Color ACTIVE_TEXT_COLOR = Color.BLACK;

    public void applyToTextField(JTextField field, String placeholder) {
        
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);


        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ACTIVE_TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    public void applyToPasswordField(JPasswordField field, String placeholder) {
        
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);


        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String currentText = String.valueOf(field.getPassword());
                if (currentText.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ACTIVE_TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = String.valueOf(field.getPassword());
                if (currentText.isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }
}
