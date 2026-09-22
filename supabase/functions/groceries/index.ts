//Get groceries by aisle

import { AISLE_ORDER, errorResponse, handleOptions, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "GET") return errorResponse("Use GET", 405);

  try {
    const { admin, userId } = await requireUser(req);

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
  } catch (error) {
    return respondToError(error);
  }
});
