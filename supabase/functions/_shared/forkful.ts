//Shared helpers

export const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
};

export function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

export function errorResponse(message: string, status = 400): Response {
  return json({ error: message }, status);
}

export function handleOptions(): Response {
  return new Response("ok", { headers: corsHeaders });
}

//Supabase clients

export async function serviceClient() {
  const { createClient } = await import(
    "npm:@supabase/supabase-js@2"
  );
  return createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    { auth: { persistSession: false } },
  );
}

export async function anonClient() {
  const { createClient } = await import(
    "npm:@supabase/supabase-js@2"
  );
  return createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_ANON_KEY")!,
    { auth: { persistSession: false } },
  );
}

//Auth user resolver

export async function requireUser(req: Request) {
  const authHeader = req.headers.get("Authorization") ?? "";
  const token = authHeader.replace("Bearer ", "").trim();
  if (!token) {
    throw new AuthError("Missing bearer token", 401);
  }
  const admin = await serviceClient();
  const { data, error } = await admin.auth.getUser(token);
  if (error || !data?.user) {
    throw new AuthError("Invalid or expired session token", 401);
  }
  const userId = data.user.id;

  const { data: profile } = await admin
    .from("users")
    .select("*")
    .eq("user_id", userId)
    .maybeSingle();

  return { admin, userId, profile };
}

export class AuthError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message);
  }
}

export function respondToError(error: unknown): Response {
  if (error instanceof AuthError) {
    return errorResponse(error.message, error.status);
  }
  console.error("[forkful] unexpected error:", error);
  return errorResponse(error instanceof Error ? error.message : "Server error", 500);
}

//Domain helpers

const AISLE_KEYWORDS: Array<[string, string[]]> = [
  ["Produce", ["tomato", "onion", "garlic", "spinach", "basil", "lettuce", "pepper", "carrot", "potato", "avocado", "lemon", "lime", "cilantro", "coriander", "ginger", "mushroom", "zucchini", "cucumber", "apple", "banana", "berrri", "berry", "beetroot", "beet", "celery", "spring onion", "chilli", "chili", "herbs", "parsley", "mint", "thyme", "rosemary", "cabbage", "mealie", "butternut", "sweet potato", "corn", "sweetcorn", "gem squash"]],
  ["Dairy", ["milk", "cheese", "mozzarella", "feta", "butter", "cream", "yoghurt", "yogurt", "egg", "cheddar", "parmesan", "buttermilk", "custard", "ghee"]],
  ["Butchery", ["chicken", "beef", "mutton", "lamb", "pork", "bacon", "mince", "sausage", "boerewors", "steak", "rib", "turkey", "liver", "tripe", "wors", "brisket"]],
  ["Seafood", ["fish", "salmon", "tuna", "prawn", "shrimp", "hake", "calamari", "mussel", "anchov", "sardine", "shellfish"]],
  ["Bakery", ["bread", "flatbread", "roll", "bun", "pita", "pastry", "croissant", "wrap", "toast", "breadcrumb", "tortilla", "scone"]],
  ["Dry Goods", ["rice", "pasta", "noodle", "flour", "sugar", "oat", "quinoa", "couscous", "lentil", "bean", "chickpea", "maize", "semolina", "popcorn", "barley"]],
  ["Pantry", ["oil", "vinegar", "soy sauce", "sauce", "stock", "broth", "honey", "syrup", "paste", "peanut butter", "jam", "tin", "canned", "coconut milk", "peri-peri", "periperi", "salsa", "tahini", "mayonnaise", "mayo", "tomato tin", "chutney", "relish"]],
  ["Spices", ["salt", "peppercorn", "paprika", "cumin", "curry", "cinnamon", "turmeric", "oregano", "masala", "nutmeg", "clove", "star anise", "cardamom", "cayenne", "bbq", "spice", "herb mix", "five-spice", "coriander seed"]],
  ["Baking", ["baking powder", "bicarbonate", "baking soda", "yeast", "cocoa", "chocolate", "vanilla", "icing sugar", "cornflour", "corn starch", "gelatin"]],
  ["Frozen", ["frozen", "ice cream"]],
  ["Beverages", ["water", "juice", "wine", "beer", "coffee", "tea", "cola"]],
];

export function aisleFor(name: string): string {
  const lower = name.toLowerCase();
  for (const [aisle, keywords] of AISLE_KEYWORDS) {
    if (keywords.some((keyword) => lower.includes(keyword))) {
      return aisle;
    }
  }
  return "Other";
}

const STOP_WORDS = new Set(["fresh", "ripe", "dried", "chopped", "large", "small", "free", "range"]);

export function normalizeIngredient(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^a-z\s-]/g, "")
    .split(/\s+/)
    .filter((word) => word.length > 2 && !STOP_WORDS.has(word))
    .join(" ")
    .trim();
}

//Pantry match test
export function pantryMatches(pantryName: string, ingredientName: string): boolean {
  const pantry = normalizeIngredient(pantryName);
  const ingredient = normalizeIngredient(ingredientName);
  if (!pantry || !ingredient) return false;
  if (pantry === ingredient) return true;
  if (pantry.length >= 4 && ingredient.includes(pantry)) return true;
  if (ingredient.length >= 4 && pantry.includes(ingredient)) return true;
  const singular = (word: string) => word.endsWith("oes") ? word.slice(0, -2) : word.endsWith("s") && !word.endsWith("ss") ? word.slice(0, -1) : word;
  return singular(pantry) === singular(ingredient);
}

export const AISLE_ORDER = [
  "Produce", "Bakery", "Butchery", "Dairy", "Deli", "Seafood", "Frozen",
  "Pantry", "Dry Goods", "Condiments", "Spices", "Baking", "Beverages", "Other",
];

export const MOODS = [
  "italian", "asian", "mexican", "braai", "comfort", "healthy", "sweets", "vegan", "pantry",
];
