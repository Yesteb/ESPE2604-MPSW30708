package model;

/**
 * Resultado de un intento de inicio de sesión, tal como lo devuelve la función
 * almacenada {@code fn_autenticar_admin}.
 *
 * <p>El conteo de intentos y el bloqueo los lleva la base de datos (tabla
 * {@code sesiones_admin}), no la interfaz: así sobreviven al cierre de la
 * ventana y son compartidos por todos los equipos que usen la misma base.</p>
 *
 * @param email       correo del administrador, o {@code null} si no se autenticó
 * @param nombre      nombre de usuario, o {@code null} si no se autenticó
 * @param autenticado {@code true} si usuario y contraseña son correctos
 * @param intentos    intentos fallidos acumulados en la ventana de tiempo
 * @param bloqueado   {@code true} si la cuenta está bloqueada temporalmente
 * @param mensaje     texto ya redactado para mostrar al usuario
 */
public record ResultadoLogin(
        String  email,
        String  nombre,
        boolean autenticado,
        int     intentos,
        boolean bloqueado,
        String  mensaje) {
}
