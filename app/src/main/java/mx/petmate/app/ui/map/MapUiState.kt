package mx.petmate.app.ui.map

import androidx.annotation.DrawableRes
import mx.petmate.app.core.ui.UiText

/** Tipos de negocio afiliado que filtra el mapa (spec 010, A-02 y A-03). */
enum class BusinessType { Vet, Shop, Spa }

/**
 * Negocio tal como lo pinta el mapa.
 *
 * [mapX] y [mapY] son la posicion sobre el mapa ilustrado (800 × 1000 dp) de
 * la maqueta. Cuando la spec 010 fije el proveedor de mapas se cambian por
 * latitud y longitud; el resto de la tarjeta no cambia.
 */
data class MapBusiness(
    val id: String,
    val name: String,
    val type: BusinessType,
    val mapX: Float,
    val mapY: Float,
    /** Distancia ya formateada, por ejemplo "600 m" o "1.1 km". */
    val distance: String,
    val isOpen: Boolean,
    /** Hora de cierre si esta abierto, o de apertura si esta cerrado; null = 24 horas. */
    val scheduleTime: String?,
    val phone: String,
    val hasWhatsApp: Boolean,
    /** Foto provisional desde recursos; con el backend sera una URL. */
    @DrawableRes val photo: Int? = null,
)

/**
 * Estado de la pantalla de inicio con el mapa (spec 010).
 *
 * - Cargando: [isLoading] con el mapa de fondo y los marcadores ocultos.
 * - Vacio: sin error, sin carga y sin negocios visibles (A-10).
 * - Error: [error] distinto de null, con opcion de reintentar (A-10).
 */
data class MapUiState(
    val isLoading: Boolean = false,
    val businesses: List<MapBusiness> = emptyList(),
    val visibleTypes: Set<BusinessType> = BusinessType.entries.toSet(),
    val query: String = "",
    val selectedBusinessId: String? = null,
    /** false cuando el permiso de ubicacion se nego o no se ha dado (A-07). */
    val isLocationAvailable: Boolean = true,
    val isHelpVisible: Boolean = false,
    val remindersCount: Int = 0,
    val error: UiText? = null,
) {
    val visibleBusinesses: List<MapBusiness> =
        businesses.filter { business ->
            business.type in visibleTypes &&
                (query.isBlank() || business.name.contains(query.trim(), ignoreCase = true))
        }

    val selectedBusiness: MapBusiness? =
        visibleBusinesses.firstOrNull { it.id == selectedBusinessId }

    val isEmpty: Boolean = !isLoading && error == null && visibleBusinesses.isEmpty()
}
