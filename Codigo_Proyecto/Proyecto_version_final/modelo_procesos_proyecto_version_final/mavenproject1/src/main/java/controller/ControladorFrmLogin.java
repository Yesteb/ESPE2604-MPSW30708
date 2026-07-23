package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import model.Administrador;
import model.ResultadoLogin;
import persistencia.AdministradorJpaController;
import view.FrmDashboard;
import view.FrmLogin;

public class ControladorFrmLogin {

    private final FrmLogin vista;
    private final SistemaAutenticacion autenticacion;

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

        new SwingWorker<ResultadoLogin, Void>() {
            @Override
            protected ResultadoLogin doInBackground() {
                return autenticacion.iniciarSesion(nombreUsuario, contrasena);
            }

            @Override
            protected void done() {
                try {
                    ResultadoLogin resultado = get();

                    if (resultado.autenticado()) {
                        abrirDashboard(autenticacion.obtenerAdministrador(resultado));
                        return;
                    }

                    // El mensaje y el conteo de intentos vienen de la base de datos:
                    // el bloqueo persiste aunque se reinicie la aplicación.
                    if (resultado.bloqueado()) {
                        JOptionPane.showMessageDialog(vista, resultado.mensaje(),
                            "Acceso bloqueado", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    } else {
                        JOptionPane.showMessageDialog(vista, resultado.mensaje(),
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                        vista.habilitarBotonLogin();
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

    private void abrirDashboard(Administrador administrador) {
        if (administrador == null) {
            JOptionPane.showMessageDialog(vista,
                "No se pudo cargar el administrador autenticado.",
                "Error", JOptionPane.ERROR_MESSAGE);
            vista.habilitarBotonLogin();
            return;
        }

        JOptionPane.showMessageDialog(vista, "Bienvenido al sistema.",
            "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);
        vista.dispose();
        try {
            new FrmDashboard(administrador).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Error al abrir el dashboard:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
