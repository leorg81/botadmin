package gub.rionegro.syslr.data;

public enum SessionStatus {
    ACTIVE,          // Sesión activa
    COMPLETED,       // Flujo completado exitosamente
    ABANDONED,       // Usuario abandonó
    TIMEOUT,         // Sesión expiró
    ERROR,           // Error en el flujo
    MENU             // En menú principal (sin flujo activo)
}