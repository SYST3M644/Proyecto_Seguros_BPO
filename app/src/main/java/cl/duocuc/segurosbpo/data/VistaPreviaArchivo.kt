package cl.duocuc.segurosbpo.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

object VistaPreviaArchivo {

    const val LADO_MINIATURA = 240
    const val LADO_COMPLETO = 1080

    fun decodificar(contenido: ByteArray, mime: String, lado: Int): Bitmap? = when {
        mime == "application/pdf" -> primeraPagina(contenido, lado)
        mime.startsWith("image/") -> imagen(contenido, lado)
        else -> null
    }

    private fun imagen(contenido: ByteArray, lado: Int): Bitmap? = runCatching {
        val medida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(contenido, 0, contenido.size, medida)

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = reduccion(maxOf(medida.outWidth, medida.outHeight), lado)
        }
        BitmapFactory.decodeByteArray(contenido, 0, contenido.size, opciones)
    }.getOrNull()

    private fun reduccion(mayor: Int, lado: Int): Int {
        if (mayor <= 0 || lado <= 0) return 1
        var factor = 1
        while (mayor / (factor * 2) >= lado) {
            factor *= 2
        }
        return factor
    }

    private fun primeraPagina(contenido: ByteArray, lado: Int): Bitmap? = runCatching {
        val temporal = File.createTempFile("evidencia", ".pdf")
        try {
            temporal.writeBytes(contenido)
            ParcelFileDescriptor.open(
                temporal, ParcelFileDescriptor.MODE_READ_ONLY
            ).use { descriptor ->
                PdfRenderer(descriptor).use { lector ->
                    if (lector.pageCount == 0) return@use null
                    lector.openPage(0).use { pagina ->
                        val factor = lado.toFloat() / maxOf(pagina.width, pagina.height)
                        val ancho = (pagina.width * factor).toInt().coerceAtLeast(1)
                        val alto = (pagina.height * factor).toInt().coerceAtLeast(1)
                        val lienzo = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
                        lienzo.eraseColor(Color.WHITE)
                        pagina.render(
                            lienzo, null, null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        lienzo
                    }
                }
            }
        } finally {
            temporal.delete()
        }
    }.getOrNull()
}
