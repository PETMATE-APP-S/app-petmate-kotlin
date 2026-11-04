package mx.petmate.app.ui.auth

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.petmate.app.core.result.AppError
import mx.petmate.app.core.result.AppResult
import mx.petmate.app.domain.model.LoginCredentials
import mx.petmate.app.domain.model.User
import mx.petmate.app.domain.usecase.LoginUseCase
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val loginUseCase: LoginUseCase = mockk()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `el estado inicial esta vacio y sin errores`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.email.isEmpty())
            assertFalse(state.isSubmitting)
            assertFalse(state.isSignedIn)
        }
    }

    @Test
    fun `un login exitoso marca la sesion como iniciada`() = runTest(dispatcher) {
        coEvery { loginUseCase(any()) } returns AppResult.Success(
            User(id = "1", name = "Adrian", email = "demo@petmate.mx"),
        )
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChanged("demo@petmate.mx")
        viewModel.onPasswordChanged("petmate123")
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSignedIn)
            assertFalse(state.isSubmitting)
        }
    }

    @Test
    fun `credenciales invalidas muestran un error general`() = runTest(dispatcher) {
        coEvery { loginUseCase(any()) } returns AppResult.Failure(AppError.Unauthorized)
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChanged("demo@petmate.mx")
        viewModel.onPasswordChanged("incorrecta")
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.generalError)
            assertFalse(state.isSignedIn)
            assertFalse(state.isSubmitting)
        }
    }

    @Test
    fun `un error de validacion del correo se muestra en su campo`() = runTest(dispatcher) {
        coEvery { loginUseCase(any()) } returns AppResult.Failure(
            AppError.Validation(
                field = "email",
                reason = mx.petmate.app.core.result.ValidationReason.INVALID_EMAIL,
            ),
        )
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChanged("malo")
        viewModel.onPasswordChanged("petmate123")
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.emailError)
            assertTrue(state.generalError == null)
        }
    }

    @Test
    fun `escribir en el correo limpia el error anterior`() = runTest(dispatcher) {
        coEvery { loginUseCase(any()) } returns AppResult.Failure(AppError.Unauthorized)
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChanged("demo@petmate.mx")
        viewModel.onPasswordChanged("mala")
        viewModel.onSubmit()
        advanceUntilIdle()
        viewModel.onEmailChanged("otro@petmate.mx")

        viewModel.uiState.test {
            assertTrue(awaitItem().generalError == null)
        }
    }

    @Test
    fun `no se envia dos veces si ya esta en curso`() = runTest(dispatcher) {
        coEvery { loginUseCase(any()) } returns AppResult.Success(
            User(id = "1", name = "Adrian", email = "demo@petmate.mx"),
        )
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChanged("demo@petmate.mx")
        viewModel.onPasswordChanged("petmate123")
        viewModel.onSubmit()
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().isSignedIn)
        }
        io.mockk.coVerify(exactly = 1) { loginUseCase(any<LoginCredentials>()) }
    }
}
