package com.emeris.forkful.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Explore : Screen("explore")
    object RecipeBox : Screen("recipe_box")
    object Pantry : Screen("pantry")
    object Basket : Screen("basket")
    object Settings : Screen("settings")
    object SwipeStack : Screen("swipe_stack")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    object Notifications : Screen("notifications")
}