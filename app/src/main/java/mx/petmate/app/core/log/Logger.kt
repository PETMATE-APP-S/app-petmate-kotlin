package mx.petmate.app.core.log

import android.util.Log
import mx.petmate.app.BuildConfig

/**
 * Registro de eventos y errores.
 *
 * Nunca debe recibir contrasenas, tokens ni datos personales
 * (constitution.md P6). En release queda en silencio.
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

class AndroidLogger : Logger {
    override fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}
