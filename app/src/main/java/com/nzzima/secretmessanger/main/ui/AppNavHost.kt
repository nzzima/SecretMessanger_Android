package com.nzzima.secretmessanger.main.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nzzima.secretmessanger.auth.ui.AuthScreen
import com.nzzima.secretmessanger.chats.ui.ChatsScreen
import com.nzzima.secretmessanger.session.domain.models.Session
import org.koin.androidx.compose.koinViewModel

/**
 * Граф навигации приложения.
 *
 * Стартовое назначение выбирается по текущему значению
 * [RootViewModel.observeSession]: [Destination.Auth] при [Session.Anonymous], иначе
 * [Destination.Chats]. Дальнейшие изменения сессии переводят граф на соответствующее
 * назначение и очищают стек возврата.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: RootViewModel = koinViewModel(),
) {
    val session by viewModel.observeSession().collectAsStateWithLifecycle()
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
            AuthScreen()
        }
        composable(Destination.Chats.route) {
            ChatsScreen()
        }
    }
}
