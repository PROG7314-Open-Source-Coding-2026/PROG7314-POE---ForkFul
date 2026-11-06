//Recipe box management

import { errorResponse, handleOptions, isUuid, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();

  try {
    const { admin, userId } = await requireUser(req);

    if (req.method === "GET") {
      const status = (new URL(req.url).searchParams.get("status") ?? "ALL").toUpperCase();

      const { data: swipes, error } = await admin
        .from("user_swipes")
        .select("recipe_id, direction, cooked_at, swiped_at, recipes(*, ingredients(name))")
        .eq("user_id", userId)
        .eq("direction", "RIGHT")
        .order("swiped_at", { ascending: false });
      if (error) return errorResponse(error.message, 500);

      const entries = (swipes ?? [])
        .filter((row) => {
          const cooked = row.cooked_at != null;
          if (status === "SAVED") return !cooked;
          if (status === "COOKED") return cooked;
          return true;
        })
        .map((row) => {
          const recipe = row.recipes as Record<string, unknown>;
          const ingredientsList = (recipe.ingredients as Array<{ name: string }> ?? []);
          return {
            recipeId: recipe.recipe_id,
            title: recipe.title,
            description: recipe.description,
            imageUrl: recipe.image_url,
            prepTimeMinutes: recipe.prep_time_minutes,
            calories: recipe.calories,
            proteinGrams: recipe.protein_grams,
            rating: Number(recipe.rating),
            difficulty: recipe.difficulty,
            cuisineType: recipe.cuisine_type,
            tags: [],
            topTagPoints: 0,
            matchPercentage: 0,
            inPantryCount: 0,
            totalIngredientsCount: ingredientsList.length,
            ingredients: ingredientsList.map((i) => ({ name: i.name, inPantry: false })),
            isSaved: true,
            isCooked: row.cooked_at != null,
          };
        });

      return json(entries);
    }

    if (req.method === "POST") {
      const body = await req.json();
      const recipeId = String(body.recipeId ?? "");
      const cooked = body.cooked !== false;
      if (!isUuid(recipeId)) return errorResponse("recipeId must be a valid id", 400);

      // Check if user already has a swipe entry for this recipe
      const { data: existingSwipe, error: checkError } = await admin
        .from("user_swipes")
        .select("swipe_id, mood_profile_key, direction")
        .eq("user_id", userId)
        .eq("recipe_id", recipeId)
        .maybeSingle();
      if (checkError) return errorResponse(checkError.message, 500);

      const cookedAt = cooked ? new Date().toISOString() : null;

      if (existingSwipe) {
        // Update existing row: set cooked_at, and promote direction to RIGHT if cooked
        const updatePayload: Record<string, unknown> = { cooked_at: cookedAt };
        if (cooked) {
          updatePayload.direction = "RIGHT";
        }
        const { error } = await admin
          .from("user_swipes")
          .update(updatePayload)
          .eq("swipe_id", existingSwipe.swipe_id);
        if (error) return errorResponse(error.message, 500);
      } else if (cooked) {
        // New entry: find mood_profile_key from recipe_tags or default to 'comfort'
        const { data: recipeTag } = await admin
          .from("recipe_tags")
          .select("mood_profile_key")
          .eq("recipe_id", recipeId)
          .limit(1)
          .maybeSingle();

        const moodKey = recipeTag?.mood_profile_key ?? "comfort";

        const { error } = await admin
          .from("user_swipes")
          .insert({
            user_id: userId,
            recipe_id: recipeId,
            mood_profile_key: moodKey,
            direction: "RIGHT",
            cooked_at: cookedAt,
            swiped_at: new Date().toISOString(),
          });
        if (error) return errorResponse(error.message, 500);
      }

      return json({ status: "ok" });
    }

    //Unsave: the recipe leaves the Recipe Box and stays out of the deck
    if (req.method === "DELETE") {
      const recipeId = new URL(req.url).searchParams.get("recipeId");
      if (!isUuid(recipeId)) return errorResponse("recipeId must be a valid id", 400);

      const { error } = await admin
        .from("user_swipes")
        .update({ direction: "LEFT", cooked_at: null })
        .eq("user_id", userId)
        .eq("recipe_id", recipeId);
      if (error) return errorResponse(error.message, 500);

      //Drop the basket items this recipe added that are not ticked off yet
      const { error: basketError } = await admin
        .from("grocery_items")
        .delete()
        .eq("user_id", userId)
        .eq("recipe_id", recipeId)
        .eq("is_checked", false);
      if (basketError) return errorResponse(basketError.message, 500);

      return json({ status: "unsaved" });
    }

    return errorResponse("Use GET, POST or DELETE", 405);
  } catch (error) {
    return respondToError(error);
  }
});

//------------------------------End-Of-File------------------------------//
