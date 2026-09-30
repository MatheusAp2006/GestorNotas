package co.edu.ue.gestorNotas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

// Utilidad para cargar miniaturas de imágenes guardadas en el teléfono.

// Declaración de la clase pública utilitaria ImagenUtil que procesa y optimiza imágenes del almacenamiento local
public class ImagenUtil {

    // Método estático para cargar una imagen submuestreada (reducida) y corregir su orientación según metadatos EXIF
    public static Bitmap cargarMiniatura(String ruta, int tamano) {
        // Instancia el objeto de opciones para configurar el proceso de decodificación de la imagen
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        // inJustDecodeBounds = true extrae únicamente las dimensiones del archivo sin cargarlo en la memoria RAM
        opciones.inJustDecodeBounds = true;
        // Lee el archivo de imagen especificado en la ruta para rellenar las propiedades de 'opciones'
        BitmapFactory.decodeFile(ruta, opciones);

        // Inicializa la variable 'escala' en 1 como factor base de submuestreo
        int escala = 1;
        // Determina el factor de escala en potencias de 2 mientras el tamaño reducido supere el tamaño deseado
        while (opciones.outWidth / (escala * 2) >= tamano
                && opciones.outHeight / (escala * 2) >= tamano) {
            escala *= 2;
        }
        // Asigna la escala calculada al parámetro inSampleSize de BitmapFactory
        opciones.inSampleSize = escala;
        // Desactiva la decodificación ligera para permitir la creación completa del objeto Bitmap en memoria
        opciones.inJustDecodeBounds = false;
        // Decodifica la imagen aplicando el factor de reducción configurado en inSampleSize
        Bitmap bitmap = BitmapFactory.decodeFile(ruta, opciones);
        // Si el archivo está dañado o no se pudo convertir a Bitmap, retorna null
        if (bitmap == null) return null;

        // Inicializa los grados de rotación requeridos en 0
        int grados = 0;
        try {
            // Instancia ExifInterface para acceder a los metadatos de orientación de la imagen
            ExifInterface exif = new ExifInterface(ruta);
            // Lee el atributo de orientación guardado por la cámara del dispositivo
            int orientacion = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            // Convierte la constante de orientación EXIF en un valor numérico en grados
            if (orientacion == ExifInterface.ORIENTATION_ROTATE_90) grados = 90;
            else if (orientacion == ExifInterface.ORIENTATION_ROTATE_180) grados = 180;
            else if (orientacion == ExifInterface.ORIENTATION_ROTATE_270) grados = 270;
        } catch (IOException e) {
            // Captura la excepción de lectura de metadatos si el archivo no cuenta con cabeceras EXIF
        }
        // Si la orientación es diferente a 0 grados, crea un nuevo Bitmap rotado
        if (grados != 0) {
            // Crea una matriz de transformación geométrica de Android
            Matrix matriz = new Matrix();
            // Aplica la rotación en sentido horario acorde a los grados detectados
            matriz.postRotate(grados);
            // Genera y retorna el nuevo Bitmap ajustado y orientado correctamente
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matriz, true);
        }
        // Retorna el Bitmap optimizado final
        return bitmap;
    }
}