// Declaración del paquete 'local' donde se define la entidad persistente para Room
package co.edu.ue.gestorNotas.local;

// Importaciones de anotaciones de Room para el mapeo objeto-relacional (ORM)
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Anotación @Entity que define esta clase como una tabla SQLite llamada "tareas_local"
@Entity(tableName = "tareas_local")
public class TareaEntity {

    // Anotación @PrimaryKey con autogeneración (autoincremental) para el identificador único de cada registro
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Atributo público que actúa como clave foránea (FK) para vincular la tarea con una materia específica
    public long idMateria;
    // Atributo para almacenar el nombre o título de la tarea
    public String titulo;
    // Atributo para los detalles o descripción adicional
    public String descripcion;
    // Atributo para la fecha límite en formato ISO "AAAA-MM-DD" (puede ser nula)
    public String fechaEntrega;
    // Atributo para el nivel de prioridad ("BAJA", "MEDIA", "ALTA")
    public String prioridad;
    // Atributo para el estado actual de la tarea ("PENDIENTE", "EN_PROCESO", "FINALIZADO")
    public String estado;

    // Constructor principal para instanciar la entidad omitiendo el 'id' para que SQLite lo asigne automáticamente
    public TareaEntity(long idMateria, String titulo, String descripcion,
                       String fechaEntrega, String prioridad, String estado) {
        this.idMateria = idMateria;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
        this.prioridad = prioridad;
        this.estado = estado;
    }

    // Método utilitario que evalúa si la tarea está vencida comparando lexicográficamente su fecha con la fecha actual recibida ("hoyIso")
    public boolean estaVencida(String hoyIso) {
        // Retorna true si la fecha existe, es menor a la fecha de hoy y la tarea aún no está en estado "FINALIZADO"
        return fechaEntrega != null
                && fechaEntrega.compareTo(hoyIso) < 0
                && !"FINALIZADO".equals(estado);
    }
}