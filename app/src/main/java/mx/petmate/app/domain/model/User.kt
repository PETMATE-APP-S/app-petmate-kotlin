package mx.petmate.app.domain.model

/** Usuario autenticado. */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val isAffiliate: Boolean = false,
)
