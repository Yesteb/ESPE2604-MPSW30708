package controller;

import javax.swing.JOptionPane;
import view.FrmCodigoVerificacion;
import view.FrmPasswordOlvidada;

public class ControladorFrmPasswordOlvidada {

    private final FrmPasswordOlvidada vista;
    private final SistemaAutenticacion sistemaAuth;

    public ControladorFrmPasswordOlvidada(FrmPasswordOlvidada vista, SistemaAutenticacion sistemaAuth) {
        this.vista = vista;
        this.sistemaAuth = sistemaAuth;
    }

    public void manejarEnvioCorreo() {
        String correo = vista.getCorreoIngresado();

        if (correo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese su correo electrónico.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!esCorreoValido(correo)) {
            JOptionPane.showMessageDialog(vista, "El formato del correo no es válido.", "Correo inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sistemaAuth.enviarCodigoRecuperacion(correo);
        JOptionPane.showMessageDialog(vista,
                "Se ha enviado un código de verificación a: " + correo,
                "Código enviado", JOptionPane.INFORMATION_MESSAGE);

        FrmCodigoVerificacion frmCodigo = new FrmCodigoVerificacion(sistemaAuth, correo);
        frmCodigo.setVisible(true);
        vista.dispose();
    }

    public void manejarCancelar() {
        vista.dispose();
    }

    private boolean esCorreoValido(String correo) {
        return correo.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}
