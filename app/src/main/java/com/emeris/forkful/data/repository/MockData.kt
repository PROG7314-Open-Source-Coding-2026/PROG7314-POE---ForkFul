package com.emeris.forkful.data.repository

import com.emeris.forkful.domain.model.GroceryItem
import com.emeris.forkful.domain.model.Ingredient
import com.emeris.forkful.domain.model.NotificationModel
import com.emeris.forkful.domain.model.NotificationType
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.Recipe

object MockData {
    val sampleRecipes = listOf(
        Recipe(
            id = "r1",
            title = "Margherita flatbread",
            description = "A quick and classic favourite featuring fresh basil, San Marzano tomatoes, and creamy fresh mozzarella.",
            imageUrl = "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?q=80&w=800&auto=format&fit=crop",
            prepTimeMinutes = 20,
            calories = 480,
            proteinGrams = 19,
            matchPercentage = 98,
            inPantryCount = 4,
            totalIngredientsCount = 5,
            rating = 4.9,
            category = "Italian",
            isSaved = true,
            isCooked = false,
            tags = listOf("Tomato +18", "Cheese +14", "Pizza +11"),
            ingredients = listOf(
                Ingredient("Flatbread base", "1 piece (store-bought or homemade)", inPantry = true),
                Ingredient("San Marzano tomatoes", "1/2 cup crushed", inPantry = true),
                Ingredient("Fresh Mozzarella", "4 oz, torn", inPantry = true),
                Ingredient("Fresh Basil", "Handful leaves", inPantry = true),
                Ingredient("Extra Virgin Olive Oil", "1 tbsp", inPantry = false)
            ),
            instructions = listOf(
                "Preheat the oven to 220°C.",
                "Spread crushed tomatoes over the flatbread base.",
                "Tear mozzarella over the top and drizzle with olive oil.",
                "Bake for 8 to 10 minutes until the edges are golden.",
                "Finish with fresh basil and serve warm."
            )
        ),
        Recipe(
            id = "r2",
            title = "Tomato bredie",
            description = "Traditional slow cooked rich South African mutton and tomato stew infused with warming spices.",
            imageUrl = "https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=800&auto=format&fit=crop",
            prepTimeMinutes = 60,
            calories = 540,
            proteinGrams = 32,
            matchPercentage = 92,
            inPantryCount = 5,
            totalIngredientsCount = 6,
            rating = 4.7,
            category = "Braai",
            isSaved = true,
            isCooked = false,
            tags = listOf("Stew +15", "Comfort +20"),
            ingredients = listOf(
                Ingredient("Stewing Mutton", "500g", inPantry = true),
                Ingredient("Ripe Tomatoes", "6 chopped", inPantry = true),
                Ingredient("Onions", "2 finely diced", inPantry = true),
                Ingredient("Potatoes", "2 quartered", inPantry = true),
                Ingredient("Cinnamon stick", "1 piece", inPantry = true),
                Ingredient("Chilli flakes", "1 tsp", inPantry = false)
            ),
            instructions = listOf(
                "Brown the mutton in a heavy pot.",
                "Add onions and cook until soft.",
                "Stir in tomatoes, cinnamon and chilli.",
                "Add potatoes and simmer until the meat is tender.",
                "Season and serve with rice or bread."
            )
        ),
        Recipe(
            id = "r3",
            title = "Beet & feta risotto",
            description = "Earthy beetroot combined with arborio rice and tangy crumbled feta for a vibrant culinary plate.",
            imageUrl = "https://images.unsplash.com/photo-1595295333158-4742f28fbd85?q=80&w=800&auto=format&fit=crop",
            prepTimeMinutes = 40,
            calories = 420,
            proteinGrams = 14,
            matchPercentage = 92,
            inPantryCount = 4,
            totalIngredientsCount = 5,
            rating = 4.6,
            category = "Vegetarian",
            isSaved = false,
            isCooked = true,
            tags = listOf("Beetroot +10", "Risotto +12"),
            ingredients = listOf(
                Ingredient("Arborio Rice", "1 cup", inPantry = true),
                Ingredient("Roasted Beetroot", "2 pureed", inPantry = true),
                Ingredient("Feta Cheese", "100g crumbled", inPantry = true),
                Ingredient("Vegetable Stock", "3 cups", inPantry = true),
                Ingredient("Shallots", "1 minced", inPantry = false)
            ),
            instructions = listOf(
                "Soften the shallots in a little butter.",
                "Stir in the rice until the grains look glossy.",
                "Add stock a ladle at a time, stirring often.",
                "Fold through the beetroot puree.",
                "Finish with crumbled feta and serve."
            )
        ),
        Recipe(
            id = "r4",
            title = "Peri-peri chicken bowls",
            description = "Spicy flame roasted marinated chicken over fresh turmeric rice and crisp fresh salad.",
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=800&auto=format&fit=crop",
            prepTimeMinutes = 25,
            calories = 510,
            proteinGrams = 42,
            matchPercentage = 98,
            inPantryCount = 4,
            totalIngredientsCount = 5,
            rating = 4.8,
            category = "Braai",
            isSaved = true,
            isCooked = false,
            tags = listOf("Chicken +22", "Spicy +14"),
            ingredients = listOf(
                Ingredient("Chicken Breasts", "350g sliced", inPantry = true),
                Ingredient("Peri-peri sauce", "3 tbsp", inPantry = true),
                Ingredient("Basmati Rice", "1 cup", inPantry = true),
                Ingredient("Avocado", "1 diced", inPantry = true),
                Ingredient("Sweetcorn", "1/2 cup", inPantry = false)
            ),
            instructions = listOf(
                "Coat the chicken in peri-peri sauce.",
                "Cook the rice until fluffy.",
                "Pan-fry or grill the chicken until cooked through.",
                "Build bowls with rice, chicken, avocado and sweetcorn.",
                "Spoon over extra peri-peri and serve."
            )
        )
    )

    val samplePantryItems = listOf(
        PantryItem("p1", "Spinach", "Produce", daysUntilExpiry = 1),
        PantryItem("p2", "Milk", "Dairy", daysUntilExpiry = 2),
        PantryItem("p3", "Mozzarella", "Dairy"),
        PantryItem("p4", "Flatbread", "Bakery"),
        PantryItem("p5", "Tomatoes", "Produce"),
        PantryItem("p6", "Olive Oil", "Condiments"),
        PantryItem("p7", "Garlic", "Produce"),
        PantryItem("p8", "Basil", "Herbs"),
        PantryItem("p9", "Balsamic Glaze", "Condiments"),
        PantryItem("p10", "Salt", "Spices"),
        PantryItem("p11", "Black Pepper", "Spices"),
        PantryItem("p12", "Onions", "Produce"),
        PantryItem("p13", "Pasta", "Dry Goods"),
        PantryItem("p14", "Eggs", "Dairy"),
        PantryItem("p15", "Flour", "Baking"),
        PantryItem("p16", "Sugar", "Baking"),
        PantryItem("p17", "Butter", "Dairy")
    )

    val sampleGroceryItems = listOf(
        GroceryItem("g1", "Fresh basil", "1 bunch", "PRODUCE"),
        GroceryItem("g2", "Tomatoes", "4 large vine", "PRODUCE"),
        GroceryItem("g3", "Mozzarella", "8 oz fresh", "DAIRY"),
        GroceryItem("g4", "Olive Oil", "Extra Virgin, 500ml", "PANTRY")
    )

    val sampleNotifications = listOf(
        NotificationModel(
            id = "n1",
            title = "Action Required",
            description = "Half a carton of Heavy Cream is nearing its end. Make a quick pan sauce before it spoils.",
            timeAgo = "2h ago",
            type = NotificationType.ACTION_REQUIRED,
            isUnread = false
        ),
        NotificationModel(
            id = "n2",
            title = "Prep Reminder",
            description = "Do not forget to take the chicken thighs out of the freezer for dinner tonight.",
            timeAgo = "5h ago",
            type = NotificationType.PREP_REMINDER,
            isUnread = false
        ),
        NotificationModel(
            id = "n3",
            title = "Pantry Match Found",
            description = "You have exactly what you need to make \"Rustic Tomato Galette\".",
            timeAgo = "1d ago",
            type = NotificationType.PANTRY_MATCH,
            isUnread = true
        )
    )
}