package com.emeris.forkful

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
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
            }
        }
    }
}

@Composable
    val navController = rememberNavController()

    NavHost(
        navController = navController,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(Screen.Explore.route) {
                    }
                }
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                },
                }
            )
        }

        composable(Screen.RecipeBox.route) {
            RecipeBoxScreen(
                onRecipeSelected = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Pantry.route) {
            PantryScreen(
                }
            )
        }

        composable(Screen.Basket.route) {
            BasketScreen(
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                    }
                }
            )
        }

            SwipeStackScreen(
                onDismiss = { navController.popBackStack() },
                onInspectRecipe = { id: String ->
                    navController.navigate(Screen.RecipeDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
        ) { backStackEntry ->
            RecipeDetailScreen(
                recipeId = recipeId,
                onClose = { navController.popBackStack() },
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}