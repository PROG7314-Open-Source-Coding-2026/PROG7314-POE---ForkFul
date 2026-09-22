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
import androidx.compose.ui.platform.LocalContext
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
import com.emeris.forkful.ui.auth.SignUpScreen
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

        val sessionManager = (application as ForkfulApplication).container.sessionManager

        setContent {
            val prefs by sessionManager.prefsFlow.collectAsState(initial = LocalPrefs())
            val isDarkTheme = when (prefs.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            ForkfulTheme(darkTheme = isDarkTheme) {
                ForkfulApp()
            }
        }
    }
}

@Composable
fun ForkfulApp() {
    val context = LocalContext.current
    val sessionManager = remember {
        (context.applicationContext as ForkfulApplication).container.sessionManager
    }

    val startDestination = remember {
        val session = sessionManager.getSession()
        when {
            session == null -> Screen.Login.route
            !sessionManager.isOnboarded() -> Screen.Onboarding.route
            else -> Screen.Explore.route
        }
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onAuthenticated = { isNewUser ->
                    val destination = if (isNewUser) Screen.Onboarding.route else Screen.Explore.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onAuthenticated = { isNewUser ->
                    val destination = if (isNewUser) Screen.Onboarding.route else Screen.Explore.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
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
                onNavigateTo = { targetRoute ->
                    navController.navigate(targetRoute)
                },
                onRecipeSelected = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onStackSelected = { moodKey ->
                    navController.navigate(Screen.SwipeStack.createRoute(moodKey))
                }
            )
        }

        composable(Screen.RecipeBox.route) {
            RecipeBoxScreen(
                onNavigateTo = { targetRoute ->
                    navController.navigate(targetRoute)
                },
                onRecipeSelected = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        composable(Screen.Pantry.route) {
            PantryScreen(
                onNavigateTo = { targetRoute ->
                    navController.navigate(targetRoute)
                },
                onRecipeSelected = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        composable(Screen.Basket.route) {
            BasketScreen(
                onNavigateTo = { targetRoute ->
                    navController.navigate(targetRoute)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateTo = { targetRoute ->
                    navController.navigate(targetRoute)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.SwipeStack.route,
            arguments = listOf(
                navArgument(Screen.SwipeStack.ARG_MOOD) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "comfort"
                }
            )
        ) { backStackEntry ->
            val mood = backStackEntry.arguments?.getString(Screen.SwipeStack.ARG_MOOD) ?: "comfort"
            SwipeStackScreen(
                moodKey = mood,
                onDismiss = { navController.popBackStack() },
                onInspectRecipe = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(
                navArgument(Screen.RecipeDetail.ARG_RECIPE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Screen.RecipeDetail.ARG_RECIPE_ID).orEmpty()
            RecipeDetailScreen(
                recipeId = recipeId,
                onClose = { navController.popBackStack() },
                onNavigateTo = { targetRoute ->
                    navController.navigate(targetRoute)
                },
                onStartCooking = { cookingRecipeId ->
                    navController.navigate(Screen.CookingMode.createRoute(cookingRecipeId))
                }
            )
        }

        composable(
            route = Screen.CookingMode.route,
            arguments = listOf(
                navArgument(Screen.CookingMode.ARG_RECIPE_ID) {
                    type = NavType.StringType
                }
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