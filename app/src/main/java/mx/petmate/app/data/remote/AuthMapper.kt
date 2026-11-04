package mx.petmate.app.data.remote

import mx.petmate.app.data.remote.dto.UserDto
import mx.petmate.app.domain.model.User

/** Traduce el transporte al modelo de dominio. */
fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    isAffiliate = isAffiliate,
)
