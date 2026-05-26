package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import model.Administrador;
import persistencia.AdministradorJpaController;
import view.FrmDashboard;
import view.FrmLogin;

public class ControladorFrmLogin {

    private static final int MAX_INTENTOS = 3;

    private final FrmLogin vista;
    private final SistemaAutenticacion autenticacion;
    private int intentosFallidos = 0;

    public ControladorFrmLogin(FrmLogin vista, FrmDashboard tablero) {
        this.vista        = vista;
        this.autenticacion = new SistemaAutenticacion(new AdministradorJpaController());
    }

    public void manejarLogin() {
        vista.deshabilitarBotonLogin();

        String nombreUsuario = vista.getUsuarioIngresado();
        String contrasena    = vista.getContrasenaIngresada();

        if (!validarCamposVacios(nombreUsuario, contrasena)) {
            vista.habilitarBotonLogin();
            return;
        }

        new SwingWorker<Administrador, Void>() {
            @Override
            protected Administrador doInBackground() {
                return autenticacion.obtenerAdministradorAutenticado(nombreUsuario, contrasena);
            }

            @Override
            protected void done() {
                try {
                    Administrador administrador = get();
                    if (administrador != null) {
                        intentosFallidos = 0;
                        JOptionPane.showMessageDialog(vista, "Bienvenido al sistema.", "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);
                        vista.dispose();
                        try {
                            new FrmDashboard(administrador).setVisible(true);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                "Error al abrir el dashboard:\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    } else {
                        intentosFallidos++;
                        int intentosRestantes = MAX_INTENTOS - intentosFallidos;
                        if (intentosFallidos >= MAX_INTENTOS) {
                            JOptionPane.showMessageDialog(vista,
                                "Ha excedido el número máximo de intentos. La aplicación se cerrará.",
                                "Acceso bloqueado", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        } else {
                            JOptionPane.showMessageDialog(vista,
                                "Usuario o contraseña incorrectos. Le quedan " + intentosRestantes + " intento(s).",
                                "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                            vista.habilitarBotonLogin();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(vista,
                        "Error de conexión:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    vista.habilitarBotonLogin();
                }
            }
        }.execute();
    }

    public void manejarSalida() {
        System.exit(0);
    }

    private boolean validarCamposVacios(String usuario, String contrasena) {
        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese su nombre de usuario.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese su contraseña.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
