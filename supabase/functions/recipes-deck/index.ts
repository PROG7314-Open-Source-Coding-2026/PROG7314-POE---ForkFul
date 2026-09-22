// GET /functions/v1/recipes-deck?mood=italian&limit=20&maxPrepTime=30&minRating=4.5&difficulty=Easy&search=pasta
// Returns the taste-profile-ranked card deck (FR-06 .. FR-11).

import { errorResponse, handleOptions, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "GET") return errorResponse("Use GET", 405);

  try {
    const { admin, userId } = await requireUser(req);
    const url = new URL(req.url);

    const limit = Math.min(Number(url.searchParams.get("limit") ?? 20) || 20, 50);
    const maxPrepTime = url.searchParams.get("maxPrepTime");
    const minRating = url.searchParams.get("minRating");
    const difficulty = url.searchParams.get("difficulty");
    const search = url.searchParams.get("search")?.trim();
    const dietaryParam = url.searchParams.get("dietaryTags");
    const dietary = dietaryParam
      ? dietaryParam.split(",").map((tag) => tag.trim()).filter(Boolean)
      : [];

    const { data: profileRow } = await admin
      .from("users")
      .select("dietary_tags")
      .eq("user_id", userId)
      .maybeSingle();
    const activeDietary = dietary.length
      ? dietary
      : (profileRow?.dietary_tags as string[] | null) ?? [];

    let mood = (url.searchParams.get("mood") ?? "").toLowerCase();
    if (!mood || mood === "null") {
      // Pick the mood the user has invested the most points in.
      const { data: moodTotals } = await admin
        .from("user_taste_profiles")
        .select("mood_profile_key, points")
        .eq("user_id", userId);
      const totals = new Map<string, number>();
      for (const row of moodTotals ?? []) {
        totals.set(row.mood_profile_key, (totals.get(row.mood_profile_key) ?? 0) + row.points);
      }
      mood = [...totals.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? "comfort";
    }

    const { data: tasteProfiles } = await admin
      .from("user_taste_profiles")
      .select("mood_profile_key, tag_name, points")
      .eq("user_id", userId)
      .eq("mood_profile_key", mood);
    const pointsByTag = new Map<string, number>();
    for (const row of tasteProfiles ?? []) {
      pointsByTag.set(row.tag_name, row.points);
    }

    let query = admin
      .from("recipes")
      .select("*, recipe_tags(mood_profile_key, tag_name), ingredients(name)")
      .order("rating", { ascending: false })
      .limit(200);

    if (maxPrepTime) query = query.lte("prep_time_minutes", Number(maxPrepTime));
    if (minRating) query = query.gte("rating", Number(minRating));
    if (difficulty) query = query.eq("difficulty", difficulty);
    if (search) {
      query = query.or(
        `title.ilike.%${search}%,description.ilike.%${search}%`,
      );
    }

    const { data: recipes, error } = await query;
    if (error) return errorResponse(error.message, 500);

    const { data: swiped } = await admin
      .from("user_swipes")
      .select("recipe_id, direction, cooked_at")
      .eq("user_id", userId);
    const savedIds = new Set(
      (swiped ?? []).filter((row) => row.direction === "RIGHT").map((row) => row.recipe_id),
    );
    const cookedIds = new Set(
      (swiped ?? [])
        .filter((row) => row.cooked_at !== null && row.cooked_at !== undefined)
        .map((row) => row.recipe_id),
    );

    const { data: pantry } = await admin
      .from("user_pantry")
      .select("ingredient_name")
      .eq("user_id", userId);
    const pantryNames = (pantry ?? []).map((row) => row.ingredient_name as string);

    const matchesDietary = (recipeDietary: string[] | null) =>
      !recipeDietary?.some((tag) => activeDietary.includes(tag));

    const scored = (recipes ?? [])
      .filter((recipe) => matchesDietary(recipe.dietary_tags as string[] | null))
      .filter((recipe) => !savedIds.has(recipe.recipe_id))
      .map((recipe) => {
        const moodTags = (recipe.recipe_tags as Array<{ mood_profile_key: string; tag_name: string }>)
          .filter((tag) => tag.mood_profile_key === mood)
          .map((tag) => tag.tag_name);
        const score = moodTags.reduce((sum, tag) => sum + (pointsByTag.get(tag) ?? 0), 0);

        const ingredients = (recipe.ingredients as Array<{ name: string }>).map((row) => row.name);
        const inPantry = ingredients.filter((name) =>
          pantryNames.some((pantryName) =>
            pantryName.toLowerCase().includes(name.toLowerCase().split(" ")[0]) ||
            name.toLowerCase().includes(pantryName.toLowerCase())
          )
        ).length;
        const total = ingredients.length;
        const matchPercentage = total === 0 ? 0 : Math.round((inPantry / total) * 100);

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
          tags: moodTags.map((tag) => {
            const points = pointsByTag.get(tag) ?? 0;
            return `${tag} ${points >= 0 ? "+" : ""}${points}`;
          }),
          topTagPoints: moodTags.reduce(
            (max, tag) => Math.max(max, pointsByTag.get(tag) ?? 0),
            0,
          ),
          matchPercentage,
          inPantryCount: inPantry,
          totalIngredientsCount: total,
          isSaved: savedIds.has(recipe.recipe_id),
          isCooked: cookedIds.has(recipe.recipe_id),
          _score: mood === "pantry" ? matchPercentage * 10 + score : score,
        };
      })
      .sort((a, b) => b._score - a._score)
      .slice(0, limit)
      .map(({ _score, ...deck }: { _score: number } & Record<string, unknown>) => deck);

    return json(scored);
  } catch (error) {
    return respondToError(error);
  }
});
