package mx.petmate.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Objetos de transporte de identidad.
 *
 * PROVISIONALES: el contrato real del backend todavia no esta definido
 * (spec 002, Q-01). Los nombres de campo son una propuesta y deben ajustarse
 * cuando Miguel publique el contrato. Mientras tanto la app usa
 * FakeAuthRepository y estos tipos no se ejercitan.
 */
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val user: UserDto,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("is_affiliate") val isAffiliate: Boolean = false,
)
