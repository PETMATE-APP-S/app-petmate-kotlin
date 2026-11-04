package mx.petmate.app.data.remote

import mx.petmate.app.data.remote.dto.AuthResponseDto
import mx.petmate.app.data.remote.dto.LoginRequestDto
import mx.petmate.app.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpoints de identidad.
 *
 * PROVISIONAL: las rutas son una propuesta; el contrato real esta pendiente
 * (spec 002, Q-01). No se consume hasta que el backend este desplegado y se
 * desactive USE_FAKE_BACKEND.
 */
interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout()
}
