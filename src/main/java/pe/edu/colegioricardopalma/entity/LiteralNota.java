package pe.edu.colegioricardopalma.entity;

/**
 * Literal grade representation following Peruvian grading scale.
 * A: Excellent (18-20) - Scholarship eligible
 * B: Good (14-17)
 * C: Regular (11-13)
 * D: Needs improvement (0-10)
 */
public enum LiteralNota {
    A("Excelente", 18, 20),
    B("Bueno", 14, 17),
    C("Regular", 11, 13),
    D("En proceso", 0, 10);

    private final String descripcion;
    private final int min;
    private final int max;

    LiteralNota(String descripcion, int min, int max) {
        this.descripcion = descripcion;
        this.min = min;
        this.max = max;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    /**
     * Calculate the literal grade from a numeric value.
     * @param nota The numeric grade (0-20)
     * @return The corresponding literal grade
     */
    public static LiteralNota fromNota(Double nota) {
        if (nota == null) {
            return null;
        }
        int rounded = (int) Math.round(nota);
        if (rounded >= 18) {
            return A;
        } else if (rounded >= 14) {
            return B;
        } else if (rounded >= 11) {
            return C;
        } else {
            return D;
        }
    }

    /**
     * Check if a grade is passing (>= 11).
     * @param nota The numeric grade
     * @return true if passing, false otherwise
     */
    public static boolean isAprobado(Double nota) {
        return nota != null && nota >= 11;
    }
}
