//Recipe detail view

import { errorResponse, handleOptions, json, pantryMatches, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "GET") return errorResponse("Use GET", 405);

  try {
    const { admin, userId } = await requireUser(req);
    const recipeId = new URL(req.url).searchParams.get("recipeId");
    if (!recipeId) return errorResponse("recipeId is required", 400);

    const { data: recipe, error } = await admin
      .from("recipes")
      .select("*, recipe_tags(mood_profile_key, tag_name), ingredients(*)")
      .eq("recipe_id", recipeId)
      .maybeSingle();
    if (error) return errorResponse(error.message, 500);
    if (!recipe) return errorResponse("Recipe not found", 404);

    const { data: pantry } = await admin
      .from("user_pantry")
      .select("ingredient_name")
      .eq("user_id", userId);
    const pantryNames = (pantry ?? []).map((row) => row.ingredient_name as string);

    const { data: swipe } = await admin
      .from("user_swipes")
      .select("direction, cooked_at")
      .eq("user_id", userId)
      .eq("recipe_id", recipeId)
      .maybeSingle();

    const ingredients = (recipe.ingredients as Array<{
      name: string;
      quantity: string | null;
      unit: string | null;
      aisle_category: string | null;
    }>).map((ingredient) => ({
      name: ingredient.name,
      quantity: ingredient.quantity,
      unit: ingredient.unit,
      aisleCategory: ingredient.aisle_category,
      inPantry: pantryNames.some((pantryName) => pantryMatches(pantryName, ingredient.name)),
    }));

    const inPantryCount = ingredients.filter((ingredient) => ingredient.inPantry).length;

    return json({
      recipeId: recipe.recipe_id,
      title: recipe.title,
      description: recipe.description,
      imageUrl: recipe.image_url,
      prepTimeMinutes: recipe.prep_time_minutes,
      cookTimeMinutes: recipe.cook_time_minutes,
      calories: recipe.calories,
      proteinGrams: recipe.protein_grams,
      rating: Number(recipe.rating),
      difficulty: recipe.difficulty,
      cuisineType: recipe.cuisine_type,
      dietaryTags: recipe.dietary_tags ?? [],
      tags: (recipe.recipe_tags as Array<{ tag_name: string }>).map((tag) => tag.tag_name),
      instructions: recipe.instructions ?? [],
      ingredients,
      isSaved: swipe?.direction === "RIGHT",
      isCooked: swipe?.cooked_at != null,
      matchPercentage: ingredients.length === 0
        ? 0
        : Math.round((inPantryCount / ingredients.length) * 100),
      inPantryCount,
      totalIngredientsCount: ingredients.length,
    });
  } catch (error) {
    return respondToError(error);
  }
});
