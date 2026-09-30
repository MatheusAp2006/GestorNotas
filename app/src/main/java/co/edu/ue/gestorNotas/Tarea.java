// Define el paquete al que pertenece esta clase
package co.edu.ue.gestorNotas;

/**
 * Clase modelo que representa la estructura de una Tarea en la aplicación.
 */
// Declaración de la clase pública Tarea que representa el modelo local para los datos de una tarea académica
public class Tarea {

    // Variable privada para almacenar el identificador único numérico de la tarea
    private long id;
    // Variable privada para almacenar el título o nombre descriptivo de la tarea
    private String nombre;
    // Variable privada para almacenar el nombre de la materia asociada a esta tarea
    private String materia;
    // Variable privada para almacenar la fecha límite de entrega en formato "YYYY-MM-DD"
    private String fecha;
    // Variable privada para almacenar los detalles o notas adicionales de la tarea
    private String descripcion;
    // Variable privada de tipo booleano para indicar el estado de la tarea (true: completada, false: pendiente)
    private boolean completada;

    // Constructor principal que inicializa todos los atributos de la tarea, incluyendo ID, descripción y estado
    public Tarea(long id, String nombre, String materia, String fecha, String descripcion, boolean completada) {
        // Asigna el identificador recibido al atributo de la instancia
        this.id = id;
        // Asigna el nombre o título recibido al atributo de la instancia
        this.nombre = nombre;
        // Asigna la materia recibida al atributo de la instancia
        this.materia = materia;
        // Asigna la fecha límite recibida al atributo de la instancia
        this.fecha = fecha;
        // Asigna la descripción recibida al atributo de la instancia
        this.descripcion = descripcion;
        // Asigna el estado de completado recibido al atributo de la instancia
        this.completada = completada;
    }

    // Constructor secundario que omite la descripción asignando una cadena vacía por defecto
    public Tarea(long id, String nombre, String materia, String fecha, boolean completada) {
        // Invoca al constructor principal pasando "" en el parámetro de descripción
        this(id, nombre, materia, fecha, "", completada);
    }

    // Constructor sobrecargado para crear tareas antes de ser registradas en base de datos (ID en -1, no completada)
    public Tarea(String nombre, String materia, String fecha) {
        // Invoca al constructor principal pasando ID predeterminado -1, descripción vacía y completada en false
        this(-1, nombre, materia, fecha, "", false);
    }

    // Método Getter que retorna el identificador único de la tarea
    public long getId() {
        return id;
    }

    // Método Setter que actualiza el identificador de la tarea
    public void setId(long id) {
        this.id = id;
    }

    // Método Getter que retorna el título o nombre de la tarea
    public String getNombre() {
        return nombre;
    }

    // Método Getter que retorna el nombre de la materia vinculada
    public String getMateria() {
        return materia;
    }

    // Método Getter que retorna la fecha límite asignada a la tarea
    public String getFecha() {
        return fecha;
    }

    // Método Getter que retorna la descripción o detalles adicionales
    public String getDescripcion() {
        return descripcion;
    }

    // Método Setter que permite actualizar o asignar una nueva descripción
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Método Getter de tipo booleano que indica si la tarea ha sido marcada como completada
    public boolean isCompletada() {
        return completada;
    }

    // Método Setter que cambia el estado de finalización de la tarea (true o false)
    public void setCompletada(boolean completada) {
        this.completada = completada;
    }
}