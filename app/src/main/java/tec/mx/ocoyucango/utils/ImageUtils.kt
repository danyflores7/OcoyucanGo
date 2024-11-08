// tec/mx/ocoyucango/utils/ImageUtils.kt
package tec.mx.ocoyucango.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import tec.mx.ocoyucango.R
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Captura una captura de pantalla del GoogleMap.
 *
 * @param googleMap Instancia de GoogleMap.
 * @return Bitmap de la captura de pantalla o null en caso de fallo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun captureMapSnapshot(googleMap: GoogleMap): Bitmap? {
    return suspendCancellableCoroutine { cont ->
        googleMap.snapshot { bitmap ->
            cont.resume(bitmap, null)
        }
    }
}

/**
 * Combina el mapa con superposiciones de texto y logo para crear una imagen compartible.
 *
 * @param context Contexto de la aplicación.
 * @param mapBitmap Bitmap del mapa capturado.
 * @param distance Distancia recorrida en kilómetros.
 * @param duration Duración del recorrido en formato legible.
 * @param date Fecha del recorrido.
 * @return Bitmap compuesto listo para ser compartido.
 */
fun createShareableImage(
    context: Context,
    mapBitmap: Bitmap,
    distance: String,
    duration: String,
    date: String? = null
): Bitmap {
    // Crear una copia mutable del bitmap del mapa
    val resultBitmap = mapBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)

    // Definir estilos de texto
    val paintTitle = Paint().apply {
        color = ContextCompat.getColor(context, R.color.Green) // Usar el color definido en colors.xml
        textSize = 80f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }

    val paintText = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }

    val paintSubText = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
        setShadowLayer(3f, 0f, 0f, Color.BLACK)
    }

    // Añadir el texto "OcoyucanGo" en verde en la parte superior
    val titleText = "OcoyucanGo"
    val titleX = 50f
    val titleY = 100f
    canvas.drawText(titleText, titleX, titleY, paintTitle)

    // Añadir el logo o nombre de la app en la parte superior (opcional)
    val logoDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground) // Reemplaza con tu logo
    logoDrawable?.let {
        val logoSize = 200
        it.setBounds(50, 150, 50 + logoSize, 150 + logoSize)
        it.draw(canvas)
    }

    // Añadir la información de distancia y duración
    val distanceText = "Distancia: $distance km"
    val durationText = "Duración: $duration"

    // Posicionar el texto en la parte inferior izquierda
    canvas.drawText(distanceText, 50f, resultBitmap.height - 150f, paintText)
    canvas.drawText(durationText, 50f, resultBitmap.height - 80f, paintText)

    // Añadir la fecha si está disponible
    date?.let {
        canvas.drawText(it, 50f, resultBitmap.height - 20f, paintSubText)
    }

    return resultBitmap
}
