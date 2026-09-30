// Declaración del paquete 'local' donde residen la base de datos y entidades de Room
package co.edu.ue.gestorNotas.local;

// Importaciones necesarias para la gestión del contexto de Android
import android.content.Context;

// Importaciones de la librería androidx.room para configurar la base de datos ORM
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Importaciones de concurrencia de Java para gestionar tareas en hilos secundarios
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Anotación @Database de Room que define las entidades asociadas (TareaEntity), la versión del esquema (1) y deshabilita la exportación del esquema en JSON
@Database(entities = {TareaEntity.class}, version = 1, exportSchema = false)
// Clase abstracta que sirve como el punto de acceso principal a la base de datos SQLite administrada por Room
public abstract class AppDatabase extends RoomDatabase {

    // Método abstracto de acceso al DAO (Data Access Object) de tareas que Room implementará automáticamente en tiempo de compilación
    public abstract TareaDao tareaDao();

    // Declaración de un ExecutorService de un solo hilo (SingleThreadExecutor) para ejecutar todas las consultas I/O en segundo plano sin bloquear el hilo de la UI
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Atributo estático y volátil (volatile) para implementar el patrón Singleton y asegurar visibilidad inmediata entre hilos de la instancia de la BD
    private static volatile AppDatabase instancia;

    // Método público estático Singleton para obtener o crear la instancia única de la base de datos local
    public static AppDatabase getInstancia(Context context) {
        // Primera comprobación rápida sin bloqueo de sincronización
        if (instancia == null) {
            // Bloque sincronizado sobre la clase para garantizar seguridad entre hilos (Thread-Safety) en entornos multihilo
            synchronized (AppDatabase.class) {
                // Segunda comprobación dentro del bloque protegido para evitar doble instanciación accidental
                if (instancia == null) {
                    // Construye e inicializa la base de datos de Room con el contexto global de la aplicación y el nombre del archivo 'tasky_local_db'
                    instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "tasky_local_db"
                    ).build();
                }
            }
        }
        // Retorna la instancia única de AppDatabase
        return instancia;
    }
}