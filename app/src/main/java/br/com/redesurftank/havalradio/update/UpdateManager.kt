package br.com.redesurftank.havalradio.update

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import br.com.redesurftank.havalradio.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Verificação e instalação de atualizações via GitHub Releases (mesmo padrão do Haval Climate Control).
 * Consulta a release "latest" do repo, compara com [BuildConfig.VERSION_NAME], baixa o APK e dispara
 * o instalador do sistema (requer REQUEST_INSTALL_PACKAGES + "instalar apps desconhecidos").
 */
object UpdateManager {
    private const val TAG = "HavalRadioUpdate"
    private const val REPO = "leandrosavn/haval-radio"
    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()

    data class Release(val version: String, val apkUrl: String?, val notes: String)

    val checking = mutableStateOf(false)
    val message = mutableStateOf<String?>(null)
    val available = mutableStateOf<Release?>(null)
    val downloading = mutableStateOf(false)
    val progress = mutableStateOf(0)

    val currentVersion: String get() = BuildConfig.VERSION_NAME

    fun checkForUpdate() {
        if (checking.value) return
        post { checking.value = true; available.value = null; message.value = "Verificando…" }
        io.execute {
            try {
                // A rede da central NÃO alcança api.github.com (DNS). Usamos só github.com:
                // o feed releases.atom dá a tag mais recente e montamos a URL do APK
                // deterministicamente (o CI sempre publica o asset como app-release.apk).
                val atom = httpGet("https://github.com/$REPO/releases.atom")
                val tag = Regex("/releases/tag/(v[^\"/<]+)").find(atom)?.groupValues?.get(1)
                    ?: throw IllegalStateException("nenhuma release encontrada no feed")
                val latest = tag.trimStart('v')
                // 1º <title> é o do feed; o 2º é o da release mais nova.
                val notes = Regex("<title>([^<]+)</title>").findAll(atom)
                    .map { it.groupValues[1] }.drop(1).firstOrNull().orEmpty()
                val apk: String? = "https://github.com/$REPO/releases/download/$tag/app-release.apk"
                val newer = isNewer(latest, currentVersion)
                post {
                    checking.value = false
                    if (newer) {
                        available.value = Release(latest, apk, notes)
                        message.value = "Nova versão $latest disponível"
                    } else {
                        available.value = null
                        message.value = "Você já está na versão mais recente ($currentVersion)"
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "checkForUpdate", e)
                post { checking.value = false; message.value = "Falha ao verificar: ${e.message}" }
            }
        }
    }

    fun downloadAndInstall(context: Context) {
        val rel = available.value ?: return
        val url = rel.apkUrl
        if (url.isNullOrBlank()) { post { message.value = "Release sem APK anexado" }; return }
        if (downloading.value) return
        val appCtx = context.applicationContext
        post { downloading.value = true; progress.value = 0; message.value = "Baixando…" }
        io.execute {
            try {
                val dir = appCtx.getExternalFilesDir(null) ?: appCtx.filesDir
                val file = File(dir, "haval-radio-${rel.version}.apk")
                downloadTo(url, file) { p -> post { progress.value = p } }
                post { downloading.value = false; message.value = "Baixado. Abrindo instalador…" }
                main.post { install(appCtx, file) }
            } catch (e: Throwable) {
                Log.e(TAG, "download", e)
                post { downloading.value = false; message.value = "Falha no download: ${e.message}" }
            }
        }
    }

    private fun install(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            Log.e(TAG, "install", e)
            post { message.value = "Falha ao abrir instalador: ${e.message}" }
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        fun parts(v: String) = v.trim().trimStart('v').split(".", "-").mapNotNull { it.toIntOrNull() }
        val l = parts(latest); val c = parts(current)
        for (i in 0 until maxOf(l.size, c.size)) {
            val a = l.getOrElse(i) { 0 }; val b = c.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/atom+xml, */*")
            setRequestProperty("User-Agent", "haval-radio")
            connectTimeout = 10_000; readTimeout = 15_000
        }
        try {
            return conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadTo(url: String, file: File, onProgress: (Int) -> Unit) {
        var conn = open(url)
        var redirects = 0
        while (conn.responseCode in 300..399 && redirects < 5) {
            val loc = conn.getHeaderField("Location") ?: break
            conn.disconnect(); conn = open(loc); redirects++
        }
        val total = conn.contentLength.toLong()
        conn.inputStream.use { input ->
            file.outputStream().use { out ->
                val buf = ByteArray(8192); var read: Int; var sum = 0L
                while (input.read(buf).also { read = it } >= 0) {
                    out.write(buf, 0, read); sum += read
                    if (total > 0) onProgress(((sum * 100) / total).toInt())
                }
            }
        }
        conn.disconnect()
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = false
            setRequestProperty("User-Agent", "haval-radio")
            connectTimeout = 10_000; readTimeout = 30_000
        }

    private fun post(block: () -> Unit) {
        main.post(block)
    }
}
