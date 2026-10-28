package com.example.tickets_colegiomarcelapaz.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.tickets_colegiomarcelapaz.data.Documento
import com.example.tickets_colegiomarcelapaz.data.DocumentoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ArchivoInfo(val nombre: String, val tipo: String, val tamano: Long)

class DocumentoRepository(
    private val context: Context,
    private val dao: DocumentoDao,
) {
    fun documentos(usuarioId: Long) = dao.listarPorUsuario(usuarioId)

    suspend fun buscar(id: Long): Documento? = dao.buscarPorId(id)

    /** Lee nombre, tipo MIME y tamaño del archivo elegido en el selector. */
    suspend fun leerInfo(uri: Uri): ArchivoInfo = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var nombre = "documento"
        var tamano = 0L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { c ->
                if (c.moveToFirst()) {
                    nombre = c.getString(0) ?: nombre
                    tamano = c.getLong(1)
                }
            }
        ArchivoInfo(nombre, resolver.getType(uri) ?: "", tamano)
    }

    /** Copia el archivo a filesDir/documentos (privado de la app) y lo registra en Room. */
    suspend fun guardar(uri: Uri, info: ArchivoInfo, usuarioId: Long): Documento =
        withContext(Dispatchers.IO) {
            val carpeta = File(context.filesDir, "documentos").apply { mkdirs() }
            val destino = File(carpeta, "${System.currentTimeMillis()}_${info.nombre.replace('/', '_')}")
            val entrada = context.contentResolver.openInputStream(uri)
                ?: error("No se pudo leer el archivo")
            entrada.use { input -> destino.outputStream().use { input.copyTo(it) } }

            val documento = Documento(
                usuarioId = usuarioId,
                nombre = info.nombre,
                tipo = info.tipo,
                tamano = destino.length(),
                fecha = System.currentTimeMillis(),
                ruta = destino.absolutePath,
            )
            documento.copy(id = dao.insertar(documento))
        }

    suspend fun eliminar(documento: Documento) {
        dao.eliminar(documento)
        withContext(Dispatchers.IO) { File(documento.ruta).delete() }
    }

    /**
     * Dibuja cada página del PDF en un Bitmap con PdfRenderer (API nativa de Android).
     * ponytail: renderiza todas las páginas de una vez; para PDFs muy largos convendría
     * renderizar por página al hacer scroll.
     */
    suspend fun renderizarPdf(ruta: String, anchoPx: Int = 1080): List<Bitmap> =
        withContext(Dispatchers.IO) {
            ParcelFileDescriptor.open(File(ruta), ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    (0 until renderer.pageCount).map { i ->
                        renderer.openPage(i).use { pagina ->
                            val alto = anchoPx * pagina.height / pagina.width
                            Bitmap.createBitmap(anchoPx, alto, Bitmap.Config.ARGB_8888).also { bmp ->
                                bmp.eraseColor(Color.WHITE)
                                pagina.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                        }
                    }
                }
            }
        }
}
