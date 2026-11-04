package mx.petmate.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.petmate.app.domain.model.SessionState
import mx.petmate.app.ui.auth.LoginScreen
import mx.petmate.app.ui.auth.RegisterScreen
import mx.petmate.app.ui.auth.WelcomeScreen
import mx.petmate.app.ui.home.HomeScreen

/**
 * Grafo unico de navegacion.
 *
 * El grafo que se muestra depende del estado de la sesion: no hay rutas de
 * identidad y de contenido mezcladas, asi que el boton atras nunca regresa al
 * login despues de autenticarse (spec 002, C-07).
 */
@Composable
fun PetMateNavHost(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    when (val state = sessionState) {
        SessionState.Checking -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        SessionState.SignedOut -> AuthGraph(modifier = modifier)

        is SessionState.SignedIn -> HomeScreen(
            userName = state.user.name,
            modifier = modifier,
        )
    }
}

@Composable
private fun AuthGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.WELCOME,
        modifier = modifier,
    ) {
        composable(Route.WELCOME) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Route.LOGIN) },
                onRegisterClick = { navController.navigate(Route.REGISTER) },
            )
        }
        composable(Route.LOGIN) {
            LoginScreen(
                // Al autenticarse cambia el estado de sesion y el grafo entero
                // se sustituye: esta pantalla no navega por su cuenta.
                onSignedIn = {},
                onGoToRegister = {
                    navController.navigate(Route.REGISTER) {
                        popUpTo(Route.WELCOME)
                    }
                },
            )
        }
        composable(Route.REGISTER) {
            RegisterScreen(
                onSignedIn = {},
                onGoToLogin = {
                    navController.navigate(Route.LOGIN) {
                        popUpTo(Route.WELCOME)
                    }
                },
            )
        }
    }
}
