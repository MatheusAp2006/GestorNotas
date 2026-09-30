// Declaración del paquete 'local' donde reside la interfaz Data Access Object (DAO) para Room
package co.edu.ue.gestorNotas.local;

// Importaciones de las anotaciones de Room para definir las operaciones SQL abstractas
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

// Importación de la clase List para manejar colecciones de datos
import java.util.List;

// Anotación @Dao que le indica a Room que esta interfaz contiene las operaciones de acceso a datos para la entidad TareaEntity
@Dao
public interface TareaDao {

    // Anotación @Insert que indica a Room que genere el código para insertar un nuevo registro en la tabla 'tareas_local'
    @Insert
    // Método que inserta una entidad TareaEntity y retorna el ID autonumerado recién generado (tipo long)
    long insertar(TareaEntity tarea);

    // Anotación @Update que le indica a Room que actualice las columnas del registro existente basándose en la llave primaria (id)
    @Update
    // Método para modificar los datos de una tarea ya guardada
    void actualizar(TareaEntity tarea);

    // Anotación @Delete que instruye a Room a eliminar el registro que coincida con la llave primaria de la entidad enviada
    @Delete
    // Método para borrar permanentemente un registro de la base de datos
    void eliminar(TareaEntity tarea);

    // Anotación @Query para escribir sentencias SQL personalizadas
    // Selecciona todos los registros de 'tareas_local' filtrando por la materia especificada (:idMateria) y ordenados del más reciente al más antiguo
    @Query("SELECT * FROM tareas_local WHERE idMateria = :idMateria ORDER BY id DESC")
    // Método que devuelve la lista de tareas filtradas por el identificador de la materia
    List<TareaEntity> listarPorMateria(long idMateria);
}