package cl.duocuc.segurosbpo.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CapturaFoto {

    private const val CARPETA = "capturas"
    private const val AUTORIDAD = ".archivos"

    fun nuevoDestino(context: Context): Uri {
        val carpeta = File(context.cacheDir, CARPETA)
        carpeta.mkdirs()
        val archivo = File(carpeta, "captura_" + System.currentTimeMillis() + ".jpg")
        return FileProvider.getUriForFile(context, context.packageName + AUTORIDAD, archivo)
    }

    fun borrar(context: Context, uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }
}
