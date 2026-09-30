package co.edu.ue.gestorNotas.api;

public class TareaRequest {
    private long idMateria;
    private String titulo;
    private String descripcion;
    private String fechaEntrega; // formato "2026-10-15" o null
    private String prioridad;    // "BAJA" | "MEDIA" | "ALTA"
    private String estado;       // "PENDIENTE" | "EN_PROCESO" | "FINALIZADO"

    public TareaRequest(long idMateria, String titulo, String descripcion,
                        String fechaEntrega, String prioridad, String estado) {
        this.idMateria = idMateria;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
        this.prioridad = prioridad;
        this.estado = estado;
    }
}