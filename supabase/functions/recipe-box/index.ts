//Recipe box management

import { errorResponse, handleOptions, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();

  try {
    const { admin, userId } = await requireUser(req);

    if (req.method === "GET") {
      const status = (new URL(req.url).searchParams.get("status") ?? "ALL").toUpperCase();

      const { data: swipes, error } = await admin
        .from("user_swipes")
        .select("recipe_id, direction, cooked_at, swiped_at, recipes(*)")
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
            totalIngredientsCount: 0,
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
      if (!recipeId) return errorResponse("recipeId is required", 400);

      const { error } = await admin
        .from("user_swipes")
        .update({ cooked_at: cooked ? new Date().toISOString() : null })
        .eq("user_id", userId)
        .eq("recipe_id", recipeId);
      if (error) return errorResponse(error.message, 500);

      return json({ status: "ok" });
    }

    return errorResponse("Use GET or POST", 405);
  } catch (error) {
    return respondToError(error);
  }
});
