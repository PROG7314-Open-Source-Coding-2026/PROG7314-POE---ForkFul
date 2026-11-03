//Basket by aisle
// GET    /functions/v1/groceries                 -> aisles with items
// POST   /functions/v1/groceries                 -> add an item by hand { name, quantity?, unit? }
// DELETE /functions/v1/groceries?scope=checked   -> remove ticked-off items
// DELETE /functions/v1/groceries?scope=all       -> empty the basket

import {
  AISLE_ORDER,
  aisleFor,
  errorResponse,
  handleOptions,
  json,
  normalizeIngredient,
  respondToError,
  requireUser,
} from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();

  try {
    const { admin, userId } = await requireUser(req);

    if (req.method === "GET") {
      const { data: items, error } = await admin
        .from("grocery_items")
        .select("*")
        .eq("user_id", userId)
        .order("created_at", { ascending: true });
      if (error) return errorResponse(error.message, 500);

      const grouped = new Map<string, Array<Record<string, unknown>>>();
      for (const item of items ?? []) {
        const aisle = item.aisle_category ?? "Other";
        if (!grouped.has(aisle)) grouped.set(aisle, []);
        grouped.get(aisle)!.push({
          itemId: item.item_id,
          name: item.ingredient_name,
          quantity: item.quantity?.toString() ?? null,
          unit: item.unit,
          isChecked: item.is_checked ?? false,
        });
      }

      const ordered = [...grouped.entries()].sort((a, b) => {
        const rank = (aisle: string) => {
          const index = AISLE_ORDER.indexOf(aisle);
          return index === -1 ? AISLE_ORDER.length : index;
        };
        return rank(a[0]) - rank(b[0]);
      });

      return json(
        ordered.map(([aisle, aisleItems]) => ({
          aisle,
          items: aisleItems,
        })),
      );
    }

    if (req.method === "POST") {
      const body = await req.json().catch(() => ({}));
      const name = String(body.name ?? "").trim();
      if (!name) return errorResponse("name is required", 400);
      if (name.length > 80) return errorResponse("name is too long", 400);

      let quantity: number | null = null;
      if (body.quantity != null && String(body.quantity).trim() !== "") {
        quantity = Number(String(body.quantity).replace(",", "."));
        if (Number.isNaN(quantity)) return errorResponse("quantity must be a number", 400);
      }
      const unit = body.unit ? String(body.unit).trim().slice(0, 20) : null;

      //Skip duplicates that are still on the list
      const { data: existing, error: existingError } = await admin
        .from("grocery_items")
        .select("ingredient_name")
        .eq("user_id", userId)
        .eq("is_checked", false);
      if (existingError) return errorResponse(existingError.message, 500);
      const key = normalizeIngredient(name);
      if (key && (existing ?? []).some((row) => normalizeIngredient(row.ingredient_name) === key)) {
        return errorResponse(`${name} is already in your basket`, 409);
      }

      const aisle = aisleFor(name);
      const { data: item, error } = await admin
        .from("grocery_items")
        .insert({
          user_id: userId,
          ingredient_name: name,
          quantity,
          unit,
          aisle_category: aisle,
        })
        .select("item_id")
        .single();
      if (error) return errorResponse(error.message, 500);

      return json({ itemId: item.item_id, aisle, status: "added" });
    }

    if (req.method === "DELETE") {
      const scope = (new URL(req.url).searchParams.get("scope") ?? "").toLowerCase();
      if (scope !== "checked" && scope !== "all") {
        return errorResponse("scope must be 'checked' or 'all'", 400);
      }

      let query = admin.from("grocery_items").delete().eq("user_id", userId);
      if (scope === "checked") query = query.eq("is_checked", true);
      const { error } = await query;
      if (error) return errorResponse(error.message, 500);

      return json({ status: "cleared" });
    }

    return errorResponse("Use GET, POST or DELETE", 405);
  } catch (error) {
    return respondToError(error);
  }
});
