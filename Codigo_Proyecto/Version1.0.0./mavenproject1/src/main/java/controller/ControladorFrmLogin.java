package controller;

import javax.swing.JOptionPane;
import view.FrmLogin;
import view.FrmPasswordOlvidada;

public class ControladorFrmLogin {

    private final FrmLogin vista;
    private final SistemaAutenticacion sistemaAuth;

    public ControladorFrmLogin(FrmLogin vista) {
        this.vista = vista;
        this.sistemaAuth = new SistemaAutenticacion();
    }

    public void manejarLogin() {
        String usuario = vista.getUsuarioIngresado();
        String password = vista.getPasswordIngresada();

        if (!validarCamposVacios(usuario, password)) {
            vista.habilitarBotonLogin();
            return;
        }

        if (sistemaAuth.login(usuario, password)) {
            JOptionPane.showMessageDialog(vista, "Bienvenido al sistema.", "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista, "Usuario o contraseña incorrectos.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            vista.habilitarBotonLogin();
        }
    }

    public void manejarRecuperarPassword() {
        FrmPasswordOlvidada frmRecuperacion = new FrmPasswordOlvidada(sistemaAuth);
        frmRecuperacion.setVisible(true);
        vista.setVisible(false);
    }

    public void manejarSalida() {
        System.exit(0);
    }

    private boolean validarCamposVacios(String usuario, String password) {
        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese su nombre de usuario.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese su contraseña.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
