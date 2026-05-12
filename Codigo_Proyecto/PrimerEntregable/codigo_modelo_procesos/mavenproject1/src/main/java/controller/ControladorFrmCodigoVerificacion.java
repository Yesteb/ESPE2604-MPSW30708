package controller;

import javax.swing.JOptionPane;
import view.FrmCodigoVerificacion;
import view.FrmLogin;

public class ControladorFrmCodigoVerificacion {

    private final FrmCodigoVerificacion vista;
    private final SistemaAutenticacion sistemaAuth;
    private final String email;

    public ControladorFrmCodigoVerificacion(FrmCodigoVerificacion vista, SistemaAutenticacion sistemaAuth, String email) {
        this.vista = vista;
        this.sistemaAuth = sistemaAuth;
        this.email = email;
    }

    public void manejarVerificacion() {
        String codigo = vista.getCodigoIngresado();
        String nuevaPassword = vista.getNuevaPasswordIngresada();
        String confirmarPassword = vista.getConfirmarPasswordIngresada();

        if (!validarCampos(codigo, nuevaPassword, confirmarPassword)) return;

        if (!sistemaAuth.verificarCodigo(codigo)) {
            JOptionPane.showMessageDialog(vista,
                    "El código ingresado es incorrecto o ha expirado.",
                    "Código inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        sistemaAuth.restablecerPassword(email, nuevaPassword);
        JOptionPane.showMessageDialog(vista,
                "La contraseña ha sido restablecida correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);

        new FrmLogin().setVisible(true);
        vista.dispose();
    }

    public void manejarCancelar() {
        new FrmLogin().setVisible(true);
        vista.dispose();
    }

    private boolean validarCampos(String codigo, String nuevaPassword, String confirmarPassword) {
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese el código de verificación.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (nuevaPassword.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese la nueva contraseña.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (nuevaPassword.length() < 6) {
            JOptionPane.showMessageDialog(vista, "La contraseña debe tener al menos 6 caracteres.", "Contraseña débil", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!nuevaPassword.equals(confirmarPassword)) {
            JOptionPane.showMessageDialog(vista, "Las contraseñas no coinciden.", "Error de validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
