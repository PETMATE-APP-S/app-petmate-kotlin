package mx.petmate.app.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Texto destinado a la interfaz.
 *
 * Permite que un ViewModel produzca mensajes sin depender de Context, lo que
 * lo hace probable en la JVM (AGENTS.md seccion 4).
 */
sealed interface UiText {
    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Dynamic(val value: String) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Resource -> stringResource(id, *args.toTypedArray())
        is Dynamic -> value
    }
}
