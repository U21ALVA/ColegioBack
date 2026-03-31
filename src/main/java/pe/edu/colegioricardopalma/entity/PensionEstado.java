package pe.edu.colegioricardopalma.entity;

/**
 * Estado de una pensión mensual
 */
public enum PensionEstado {
    PENDIENTE,    // Pendiente de pago
    PAGADO,       // Completamente pagado
    VENCIDO,      // Vencido y no pagado
    PARCIAL       // Pago parcial realizado
}
