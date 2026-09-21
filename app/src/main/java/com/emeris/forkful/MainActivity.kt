package com.emeris.forkful

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emeris.forkful.core.designsystem.ForkfulTheme
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.ui.auth.LoginScreen
import com.emeris.forkful.ui.auth.OnboardingScreen
import com.emeris.forkful.ui.basket.BasketScreen
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

        setContent {
            ForkfulTheme(darkTheme = false) {
                ForkfulApp()
            }
        }
    }
}

@Composable
fun ForkfulApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onContinueWithGoogle = {
                    navController.navigate(Screen.Onboarding.route)
                },
                onContinueWithEmail = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(Screen.Explore.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onNavigateTo = { route: String ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                },
                onStackSelected = {
                    navController.navigate(Screen.SwipeStack.route)
                }
            )
        }

        composable(Screen.RecipeBox.route) {
            RecipeBoxScreen(
                onNavigateTo = { route: String ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Pantry.route) {
            PantryScreen(
                onNavigateTo = { route: String ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Basket.route) {
            BasketScreen(
                onNavigateTo = { route: String ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateTo = { route: String ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SwipeStack.route) {
            SwipeStackScreen(
                onDismiss = { navController.popBackStack() },
                onInspectRecipe = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: "r1"
            RecipeDetailScreen(
                recipeId = recipeId,
                onClose = { navController.popBackStack() },
                onNavigateTo = { route: String -> navController.navigate(route) }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}