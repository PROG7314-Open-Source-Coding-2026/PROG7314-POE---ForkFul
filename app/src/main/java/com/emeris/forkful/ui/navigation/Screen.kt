package com.emeris.forkful.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Explore : Screen("explore")
    object RecipeBox : Screen("recipe_box")
    object Pantry : Screen("pantry")
    object Basket : Screen("basket")
    object Settings : Screen("settings")

    object SwipeStack : Screen("swipe_stack?mood={mood}") {
        const val ARG_MOOD = "mood"
        fun createRoute(mood: String? = null): String =
            if (mood.isNullOrBlank()) "swipe_stack" else "swipe_stack?mood=$mood"
    }

    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        const val ARG_RECIPE_ID = "recipeId"
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }

    object CookingMode : Screen("cooking_mode/{recipeId}") {
        const val ARG_RECIPE_ID = "recipeId"
        fun createRoute(recipeId: String) = "cooking_mode/$recipeId"
    }

    object Notifications : Screen("notifications")
}
