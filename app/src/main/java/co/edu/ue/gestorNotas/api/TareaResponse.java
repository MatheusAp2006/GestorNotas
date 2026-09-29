package co.edu.ue.gestorNotas.api;

public class TareaResponse {
    private long id;
    private long idMateria;
    private String titulo;
    private String descripcion;
    private String fechaEntrega;
    private String prioridad;
    private String estado;
    private String uriFoto;

    public long getId() { return id; }
    public long getIdMateria() { return idMateria; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFechaEntrega() { return fechaEntrega; }
    public String getPrioridad() { return prioridad; }
    public String getEstado() { return estado; }
    public String getUriFoto() { return uriFoto; }

    // Vencida: tiene fecha, ya pasó, y no está finalizada
    public boolean estaVencida(String hoyIso) {
        return fechaEntrega != null
                && fechaEntrega.compareTo(hoyIso) < 0
                && !"FINALIZADO".equals(estado);
    }
}