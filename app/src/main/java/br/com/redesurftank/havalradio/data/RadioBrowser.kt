package br.com.redesurftank.havalradio.data

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SONDA (probe) de reverse-engineering do MediaBrowserService do rádio AOSP
 * (com.android.car.radio/.RadioService). Conecta como cliente, percorre a árvore de browse
 * inteira e grava num arquivo em getExternalFilesDir, junto com o estado da MediaSession.
 *
 * Objetivo: descobrir o nó de favoritos/presets e o formato dos mediaId, pra então implementar
 * a leitura de favoritos + transport (play/pause) de forma definitiva. Build de release strippa
 * Log → por isso tudo vai pro arquivo (que puxamos via adb).
 */
object RadioBrowser {
    private const val TAG = "HavalRadioBrowse"
    private val SERVICE = ComponentName("com.android.car.radio", "com.android.car.radio.RadioService")
    private const val MAX_DEPTH = 3
    private val main = Handler(Looper.getMainLooper())

    private var browser: MediaBrowserCompat? = null
    private var sb = StringBuilder()
    private var pending = 0
    private var done = false
    private var outFile: File? = null

    fun probe(context: Context) {
        val appCtx = context.applicationContext
        val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val dir = appCtx.getExternalFilesDir(null) ?: appCtx.filesDir
        outFile = File(dir, "mediabrowse-$ts.txt")
        sb = StringBuilder()
        pending = 0
        done = false
        line("# RadioBrowser probe $ts")
        line("# target=$SERVICE")

        val cb = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                val b = browser ?: return
                line("CONNECTED root='${b.root}'")
                runCatching {
                    val token = b.sessionToken
                    line("sessionToken=$token")
                    val ctrl = MediaControllerCompat(appCtx, token)
                    line("  playbackState=${ctrl.playbackState}")
                    line("  metadata=${ctrl.metadata?.description}")
                    line("  flags=${ctrl.flags} pkg=${ctrl.packageName}")
                }.onFailure { line("  controller ERRO: ${it.message}") }
                subscribe(b.root, 0)
                // failsafe: fecha e grava depois de 8s mesmo se algum nó não responder
                main.postDelayed({ finish("timeout") }, 8000)
            }

            override fun onConnectionFailed() { line("CONNECTION_FAILED"); finish("failed") }
            override fun onConnectionSuspended() { line("CONNECTION_SUSPENDED"); finish("suspended") }
        }

        runCatching {
            browser = MediaBrowserCompat(appCtx, SERVICE, cb, null).also { it.connect() }
        }.onFailure { line("connect ERRO: ${it.message}"); finish("connect-error") }
    }

    private fun subscribe(parentId: String, depth: Int) {
        if (depth > MAX_DEPTH) return
        val b = browser ?: return
        pending++
        b.subscribe(parentId, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
                line("CHILDREN of '$parentId' (${children.size}) depth=$depth")
                for (c in children) {
                    val d = c.description
                    line("  [${if (c.isBrowsable) "B" else " "}${if (c.isPlayable) "P" else " "}] id='${c.mediaId}' title='${d.title}' sub='${d.subtitle}' extras=${dumpExtras(d.extras)}")
                }
                for (c in children) {
                    if (c.isBrowsable && c.mediaId != null) subscribe(c.mediaId!!, depth + 1)
                }
                pending--
                if (pending <= 0) finish("drained")
            }

            override fun onError(parentId: String) {
                line("SUBSCRIBE_ERROR '$parentId'")
                pending--
                if (pending <= 0) finish("drained-err")
            }
        })
    }

    private fun dumpExtras(b: Bundle?): String {
        if (b == null) return "null"
        return runCatching {
            b.keySet().joinToString(",", "{", "}") { k -> "$k=${b.get(k)}" }
        }.getOrDefault("?")
    }

    private fun finish(reason: String) {
        if (done) return
        done = true
        line("# FINISH ($reason)")
        runCatching { browser?.disconnect() }
        val f = outFile ?: return
        runCatching { f.writeText(sb.toString()) }
            .onSuccess { Log.i(TAG, "dump: ${f.absolutePath}") }
            .onFailure { Log.e(TAG, "write falhou", it) }
    }

    private fun line(s: String) {
        sb.append(s).append('\n')
        Log.i(TAG, s)
    }
}
