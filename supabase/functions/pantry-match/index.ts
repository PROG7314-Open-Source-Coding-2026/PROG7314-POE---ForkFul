// POST /functions/v1/pantry-match { missingThreshold: 2 }
// Runs the Capture Fridge matching algorithm (FR-20): every recipe is scored
// against the user's pantry; recipes missing at most `missingThreshold`
// ingredients are returned ordered by match percentage, with recipes that use
// soon-expiring pantry stock boosted to the top.

import { errorResponse, handleOptions, json, pantryMatches, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "POST") return errorResponse("Use POST", 405);

  try {
    const { admin, userId } = await requireUser(req);

    let missingThreshold = 2;
    try {
      const body = await req.json();
      if (body?.missingThreshold != null) {
        missingThreshold = Math.max(0, Math.min(Number(body.missingThreshold), 10));
      }
    } catch (_) {
      // empty body is fine - default threshold
    }

    const { data: pantry } = await admin
      .from("user_pantry")
      .select("ingredient_name, expiry_date")
      .eq("user_id", userId);
    const pantryRows = pantry ?? [];
    if (pantryRows.length === 0) return json([]);

    const { data: profileRow } = await admin
      .from("users")
      .select("dietary_tags")
      .eq("user_id", userId)
      .maybeSingle();
    const dietary = (profileRow?.dietary_tags as string[] | null) ?? [];

    const { data: recipes, error } = await admin
      .from("recipes")
      .select("*, ingredients(*)")
      .limit(200);
    if (error) return errorResponse(error.message, 500);

    const today = new Date().toISOString().slice(0, 10);

    const results = (recipes ?? [])
      .filter((recipe) =>
        !(recipe.dietary_tags as string[] | null)?.some((tag) => dietary.includes(tag))
      )
      .map((recipe) => {
        const ingredients = recipe.ingredients as Array<{
          name: string;
          quantity: string | null;
          unit: string | null;
          aisle_category: string | null;
        }>;

        const missing = ingredients.filter((ingredient) =>
          !pantryRows.some((row) => pantryMatches(row.ingredient_name, ingredient.name))
        );
        const matched = ingredients.length - missing.length;
        const matchPercentage = ingredients.length === 0
          ? 0
          : Math.round((matched / ingredients.length) * 100);

        // Expiry priority: count matched pantry items expiring within 3 days.
        const expiringUsed = pantryRows.filter((row) => {
          if (!row.expiry_date) return false;
          const days = Math.round(
            (new Date(row.expiry_date).getTime() - new Date(today).getTime()) / 86_400_000,
          );
          return days <= 3 && ingredients.some((ingredient) =>
            pantryMatches(row.ingredient_name, ingredient.name)
          );
        }).length;

        return {
          recipe: {
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
            matchPercentage,
            inPantryCount: matched,
            totalIngredientsCount: ingredients.length,
            isSaved: false,
            isCooked: false,
          },
          matchPercentage,
          missingIngredients: missing.map((ingredient) => ({
            name: ingredient.name,
            quantity: ingredient.quantity?.toString() ?? null,
            unit: ingredient.unit,
            aisleCategory: ingredient.aisle_category,
            inPantry: false,
          })),
          _expiring: expiringUsed,
        };
      })
      .filter((result) => result.missingIngredients.length <= missingThreshold)
      .sort((a, b) =>
        b._expiring - a._expiring || b.matchPercentage - a.matchPercentage
      )
      .slice(0, 10)
      .map(({ _expiring, ...result }: { _expiring: number } & Record<string, unknown>) => result);

    return json(results);
  } catch (error) {
    return respondToError(error);
  }
});
