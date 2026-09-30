package co.edu.ue.gestorNotas.Database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class TareaBDHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "GestorNotas.db";
    private static final int VERSION_BD = 4;

    // Tablas de Apuntes e Imágenes (ambas se relacionan con la materia por materia_id)
    public static final String TABLA_APUNTES = "apuntes";
    public static final String TABLA_IMAGENES = "imagenes";
    public static final String COL_ID = "id";
    public static final String COL_FECHA = "fecha";
    public static final String COL_MATERIA_FK = "materia_id";
    public static final String COL_CONTENIDO = "contenido";
    public static final String COL_RUTA = "ruta";

    public TareaBDHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablasApuntes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            crearTablasApuntes(db);
        }
    }

    // Apuntes e imagenes por materia
    private void crearTablasApuntes(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLA_APUNTES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MATERIA_FK + " INTEGER NOT NULL, " +
                COL_CONTENIDO + " TEXT NOT NULL, " +
                COL_FECHA + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLA_IMAGENES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MATERIA_FK + " INTEGER NOT NULL, " +
                COL_RUTA + " TEXT NOT NULL, " +
                COL_FECHA + " TEXT NOT NULL)");
    }

    private String fechaHoraActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
    }

    // INSERT INTO apuntes es el que devuelve el id insertado o -1 si hubo error.
    public long insertarApunte(long materiaId, String contenido) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MATERIA_FK, materiaId);
        values.put(COL_CONTENIDO, contenido);
        values.put(COL_FECHA, fechaHoraActual());
        return db.insert(TABLA_APUNTES, null, values);
    }

    // El INSERT INTO imagenes es el que guarda la RUTA del archivo.
    public long insertarImagen(long materiaId, String ruta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MATERIA_FK, materiaId);
        values.put(COL_RUTA, ruta);
        values.put(COL_FECHA, fechaHoraActual());
        return db.insert(TABLA_IMAGENES, null, values);
    }

    // Apuntes de una materia, desde el más reciente al más antiguo.
    public List<String> obtenerApuntes(long materiaId) {
        return obtenerColumnaDeMateria(TABLA_APUNTES, COL_CONTENIDO, materiaId);
    }

    // Rutas de las imágenes de una materia, desde la más reciente a la más antigua.
    public List<String> obtenerImagenes(long materiaId) {
        return obtenerColumnaDeMateria(TABLA_IMAGENES, COL_RUTA, materiaId);
    }

    private List<String> obtenerColumnaDeMateria(String tabla, String columna, long materiaId) {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tabla, new String[]{columna},
                COL_MATERIA_FK + " = ?", new String[]{String.valueOf(materiaId)},
                null, null, COL_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursor.getString(0));
            }
            cursor.close();
        }
        return lista;
    }
}
