package mx.petmate.app.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import mx.petmate.app.core.log.Logger
import mx.petmate.app.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las credenciales de sesion cifradas en el dispositivo.
 *
 * Usa EncryptedSharedPreferences, respaldada por el Keystore del sistema
 * (constitution.md P6, spec 002 C-12).
 *
 * Si el cifrado falla al inicializarse, se degrada de forma controlada: se
 * registra y la app pide iniciar sesion de nuevo, en lugar de caer o de
 * guardar en texto claro (spec 002, G-05).
 */
@Singleton
class SessionStore @Inject constructor(
    private val context: Context,
    private val logger: Logger,
) {

    private val prefs: SharedPreferences? by lazy {
        runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            ) as SharedPreferences
        }.onFailure {
            logger.error(TAG, "No se pudo abrir el almacenamiento cifrado", it)
        }.getOrNull()
    }

    fun save(user: User, accessToken: String, refreshToken: String) {
        prefs?.edit()
            ?.putString(KEY_USER_ID, user.id)
            ?.putString(KEY_NAME, user.name)
            ?.putString(KEY_EMAIL, user.email)
            ?.putBoolean(KEY_AFFILIATE, user.isAffiliate)
            ?.putString(KEY_ACCESS_TOKEN, accessToken)
            ?.putString(KEY_REFRESH_TOKEN, refreshToken)
            ?.apply()
    }

    fun readUser(): User? {
        val store = prefs ?: return null
        val id = store.getString(KEY_USER_ID, null) ?: return null
        val token = store.getString(KEY_ACCESS_TOKEN, null) ?: return null
        if (token.isBlank()) return null
        return User(
            id = id,
            name = store.getString(KEY_NAME, "").orEmpty(),
            email = store.getString(KEY_EMAIL, "").orEmpty(),
            isAffiliate = store.getBoolean(KEY_AFFILIATE, false),
        )
    }

    fun readAccessToken(): String? = prefs?.getString(KEY_ACCESS_TOKEN, null)

    /** Borra todo rastro de la sesion. Se llama siempre al cerrar sesion. */
    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }

    private companion object {
        const val TAG = "SessionStore"
        const val FILE_NAME = "petmate_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_AFFILIATE = "is_affiliate"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
