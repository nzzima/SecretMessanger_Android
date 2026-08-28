package com.nzzima.secretmessanger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nzzima.secretmessanger.data.session.Session
import com.nzzima.secretmessanger.di.AppContainer
import com.nzzima.secretmessanger.ui.auth.AuthScreen
import com.nzzima.secretmessanger.ui.auth.AuthViewModel
import com.nzzima.secretmessanger.ui.chats.ChatsScreen

/**
 * Граф навигации приложения.
 *
 * Стартовое назначение выбирается по текущему значению
 * [com.nzzima.secretmessanger.data.session.SessionReader.session]: [Destination.Auth] при
 * [Session.Anonymous], иначе [Destination.Chats]. Дальнейшие изменения сессии переводят
 * граф на соответствующее назначение и очищают стек возврата.
 */
@Composable
fun AppNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val session by container.sessionReader.session.collectAsStateWithLifecycle()
    val target = if (session is Session.Anonymous) Destination.Auth else Destination.Chats

    LaunchedEffect(target) {
        if (navController.currentDestination?.route != target.route) {
            navController.navigate(target.route) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = target.route,
        modifier = modifier,
    ) {
        composable(Destination.Auth.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory { initializer { AuthViewModel(container.registerAccount, container.authenticator) } },
            )
            AuthScreen(viewModel = authViewModel)
        }
        composable(Destination.Chats.route) {
            ChatsScreen(onSignOut = container.sessionCloser::signOut)
        }
    }
}
