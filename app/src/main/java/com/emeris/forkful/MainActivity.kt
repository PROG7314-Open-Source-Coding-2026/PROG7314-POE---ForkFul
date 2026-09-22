package com.emeris.forkful

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emeris.forkful.core.designsystem.ForkfulTheme
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.session.LocalPrefs
import com.emeris.forkful.ui.auth.LoginScreen
import com.emeris.forkful.ui.auth.OnboardingScreen
import com.emeris.forkful.ui.basket.BasketScreen
import com.emeris.forkful.ui.cooking.CookingModeScreen
import com.emeris.forkful.ui.explore.ExploreScreen
import com.emeris.forkful.ui.navigation.Screen
import com.emeris.forkful.ui.notifications.NotificationsScreen
import com.emeris.forkful.ui.pantry.PantryScreen
import com.emeris.forkful.ui.recipebox.RecipeBoxScreen
import com.emeris.forkful.ui.recipedetail.RecipeDetailScreen
import com.emeris.forkful.ui.settings.SettingsScreen
import com.emeris.forkful.ui.swipestack.SwipeStackScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ForkfulLogger.logLifecycle("MainActivity", "ON_CREATE")

        val container = (application as ForkfulApplication).container

        setContent {
            val prefs by container.sessionManager.prefsFlow.collectAsState(initial = LocalPrefs())
            val darkTheme = when (prefs.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            ForkfulTheme(darkTheme = darkTheme) {
                ForkfulApp(container)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ForkfulLogger.logLifecycle("MainActivity", "ON_START")
    }

    override fun onStop() {
        super.onStop()
        ForkfulLogger.logLifecycle("MainActivity", "ON_STOP")
    }
}

@Composable
fun ForkfulApp(container: AppContainer) {
    val navController = rememberNavController()

    //FR-02/FR-05 start dest check
    val startDestination = remember {
        val session = container.sessionManager.getSession()
        when {
            session == null -> Screen.Login.route
            !container.sessionManager.isOnboarded() -> Screen.Onboarding.route
            else -> Screen.Explore.route
        }
    }

    val navigateToTopLevel: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                popUpTo(Screen.Explore.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onAuthenticated = { isNewUser: Boolean ->
                    val destination = if (isNewUser) Screen.Onboarding.route else Screen.Explore.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(Screen.Explore.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onNavigateTo = navigateToTopLevel,
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                },
                onStackSelected = { mood: String ->
                    navController.navigate(Screen.SwipeStack.createRoute(mood))
                }
            )
        }

        composable(Screen.RecipeBox.route) {
            RecipeBoxScreen(
                onNavigateTo = navigateToTopLevel,
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Pantry.route) {
            PantryScreen(
                onNavigateTo = navigateToTopLevel,
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Basket.route) {
            BasketScreen(
                onNavigateTo = navigateToTopLevel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateTo = navigateToTopLevel,
                onLogout = {
                    container.sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Explore.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.SwipeStack.route,
            arguments = listOf(
                navArgument(Screen.SwipeStack.ARG_MOOD) {
                    type = NavType.StringType
                    defaultValue = "comfort"
                }
            )
        ) { backStackEntry ->
            val mood = backStackEntry.arguments?.getString(Screen.SwipeStack.ARG_MOOD) ?: "comfort"
            SwipeStackScreen(
                moodKey = mood,
                onDismiss = { navController.popBackStack() },
                onInspectRecipe = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(
                navArgument(Screen.RecipeDetail.ARG_RECIPE_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Screen.RecipeDetail.ARG_RECIPE_ID).orEmpty()
            RecipeDetailScreen(
                recipeId = recipeId,
                onClose = { navController.popBackStack() },
                onNavigateTo = navigateToTopLevel,
                onStartCooking = { id: String ->
                    navController.navigate(Screen.CookingMode.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.CookingMode.route,
            arguments = listOf(
                navArgument(Screen.CookingMode.ARG_RECIPE_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Screen.CookingMode.ARG_RECIPE_ID).orEmpty()
            CookingModeScreen(
                recipeId = recipeId,
                onFinished = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
