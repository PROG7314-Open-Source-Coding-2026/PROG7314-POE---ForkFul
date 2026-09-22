// POST /functions/v1/swipes
// Records a swipe, updates taste-profile tag points and (on RIGHT) populates
// the grocery list minus pantry stock (FR-07, FR-08, FR-21, FR-22).
// Request : { recipeId, moodProfileId, direction: "RIGHT"|"LEFT" }
// Response: { status, swipeId, updatedTagPoints: [{ moodProfileKey, tagName, points }] }

import { errorResponse, handleOptions, json, pantryMatches, respondToError, requireUser } from "../_shared/forkful.ts";

const RIGHT_DELTA = 2;
const LEFT_DELTA = -1;
const POINT_FLOOR = -50;

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "POST") return errorResponse("Use POST", 405);

  try {
    const { admin, userId } = await requireUser(req);
    const body = await req.json();

    const recipeId = String(body.recipeId ?? "");
    const moodProfileId = String(body.moodProfileId ?? "comfort").toLowerCase();
    const direction = String(body.direction ?? "").toUpperCase();
    if (!recipeId) return errorResponse("recipeId is required", 400);
    if (direction !== "RIGHT" && direction !== "LEFT") {
      return errorResponse("direction must be RIGHT or LEFT", 400);
    }

    const { data: recipe, error: recipeError } = await admin
      .from("recipes")
      .select("recipe_id, recipe_tags(mood_profile_key, tag_name), ingredients(*)")
      .eq("recipe_id", recipeId)
      .maybeSingle();
    if (recipeError) return errorResponse(recipeError.message, 500);
    if (!recipe) return errorResponse("Recipe not found", 404);

    const { data: swipe, error: swipeError } = await admin
      .from("user_swipes")
      .upsert(
        {
          user_id: userId,
          recipe_id: recipeId,
          mood_profile_key: moodProfileId,
          direction,
          swiped_at: new Date().toISOString(),
        },
        { onConflict: "user_id,recipe_id" },
      )
      .select("swipe_id")
      .single();
    if (swipeError) return errorResponse(swipeError.message, 500);

    const moodTags = (recipe.recipe_tags as Array<{ mood_profile_key: string; tag_name: string }>)
      .filter((tag) => tag.mood_profile_key === moodProfileId)
      .map((tag) => tag.tag_name);

    const delta = direction === "RIGHT" ? RIGHT_DELTA : LEFT_DELTA;
    const updatedTagPoints: Array<{ moodProfileKey: string; tagName: string; points: number }> = [];

    for (const tagName of moodTags) {
      const { data: existing } = await admin
        .from("user_taste_profiles")
        .select("profile_id, points")
        .eq("user_id", userId)
        .eq("mood_profile_key", moodProfileId)
        .eq("tag_name", tagName)
        .maybeSingle();

      const currentPoints = existing?.points ?? 0;
      const nextPoints = Math.max(currentPoints + delta, POINT_FLOOR);

      if (existing) {
        await admin
          .from("user_taste_profiles")
          .update({ points: nextPoints })
          .eq("profile_id", existing.profile_id);
      } else {
        await admin.from("user_taste_profiles").insert({
          user_id: userId,
          mood_profile_key: moodProfileId,
          tag_name: tagName,
          points: nextPoints,
        });
      }

      updatedTagPoints.push({
        moodProfileKey: moodProfileId,
        tagName,
        points: nextPoints,
      });
    }

    // RIGHT swipes feed the Smart Grocery Aggregator (FR-21, FR-22).
    let groceriesAdded = 0;
    if (direction === "RIGHT") {
      const { data: pantry } = await admin
        .from("user_pantry")
        .select("ingredient_name")
        .eq("user_id", userId);
      const pantryNames = (pantry ?? []).map((row) => row.ingredient_name as string);

      const { data: basket } = await admin
        .from("grocery_items")
        .select("ingredient_name")
        .eq("user_id", userId);
      const basketNames = new Set(
        (basket ?? []).map((row) => (row.ingredient_name as string).toLowerCase()),
      );

      const ingredients = recipe.ingredients as Array<{
        name: string;
        quantity: string | null;
        unit: string | null;
        aisle_category: string | null;
      }>;

      const missing = ingredients.filter((ingredient) => {
        const inPantry = pantryNames.some((pantryName) =>
          pantryMatches(pantryName, ingredient.name)
        );
        const inBasket = basketNames.has(ingredient.name.toLowerCase());
        return !inPantry && !inBasket;
      });

      if (missing.length > 0) {
        const rows = missing.map((ingredient) => ({
          user_id: userId,
          ingredient_name: ingredient.name,
          quantity: ingredient.quantity,
          unit: ingredient.unit,
          aisle_category: ingredient.aisle_category,
        }));
        const { error: groceryError } = await admin.from("grocery_items").insert(rows);
        if (groceryError) return errorResponse(groceryError.message, 500);
        groceriesAdded = rows.length;
      }
    }

    return json({
      status: "recorded",
      swipeId: swipe.swipe_id,
      updatedTagPoints,
      groceriesAdded,
    });
  } catch (error) {
    return respondToError(error);
  }
});
