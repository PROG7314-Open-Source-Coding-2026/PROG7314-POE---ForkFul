package com.emeris.forkful.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emeris.forkful.ForkfulApplication
import com.emeris.forkful.ui.auth.LoginScreen
import com.emeris.forkful.ui.auth.OnboardingScreen
import com.emeris.forkful.ui.auth.SignUpScreen
import com.emeris.forkful.ui.basket.BasketScreen
import com.emeris.forkful.ui.cooking.CookingModeScreen
import com.emeris.forkful.ui.explore.ExploreScreen
import com.emeris.forkful.ui.notifications.NotificationsScreen
import com.emeris.forkful.ui.pantry.PantryScreen
import com.emeris.forkful.ui.recipebox.RecipeBoxScreen
import com.emeris.forkful.ui.recipedetail.RecipeDetailScreen
import com.emeris.forkful.ui.settings.SettingsScreen
import com.emeris.forkful.ui.swipestack.SwipeStackScreen

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

    // Expired session from any screen -> back to Login with a message
    LaunchedEffect(Unit) {
        sessionManager.sessionExpired.collect {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val notice = remember { sessionManager.consumeLoginMessage() }
            LoginScreen(
                notice = notice,
                onAuthenticated = { needsOnboarding ->
                    val destination = if (needsOnboarding) Screen.Onboarding.route else Screen.Explore.route
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
                onAuthenticated = { needsOnboarding ->
                    val destination = if (needsOnboarding) Screen.Onboarding.route else Screen.Explore.route
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

//------------------------------End-Of-File------------------------------//
