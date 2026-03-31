package pe.edu.colegioricardopalma.entity;

/**
 * Estado de un pago/transacción
 */
public enum PagoEstado {
    PENDIENTE,     // Pendiente (checkout creado pero no completado)
    COMPLETADO,    // Pago exitoso
    FALLIDO,       // Pago fallido
    REEMBOLSADO    // Pago reembolsado
}
