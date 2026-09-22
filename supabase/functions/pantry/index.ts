//Pantry management function

import { aisleFor, errorResponse, handleOptions, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();

  try {
    const { admin, userId } = await requireUser(req);

    if (req.method === "GET") {
      const { data: items, error } = await admin
        .from("user_pantry")
        .select("*")
        .eq("user_id", userId)
        .order("created_at", { ascending: false });
      if (error) return errorResponse(error.message, 500);

      return json(
        (items ?? []).map((item) => ({
          pantryItemId: item.pantry_item_id,
          name: item.ingredient_name,
          quantity: item.quantity?.toString() ?? null,
          unit: item.unit,
          expiryDate: item.expiry_date,
          category: aisleFor(item.ingredient_name),
        })),
      );
    }

    if (req.method === "POST") {
      const body = await req.json();
      const name = String(body.name ?? "").trim();
      if (!name) return errorResponse("name is required", 400);
      if (name.length > 80) return errorResponse("name is too long", 400);

      const expiryDate = body.expiryDate ? String(body.expiryDate) : null;
      if (expiryDate && !/^\d{4}-\d{2}-\d{2}$/.test(expiryDate)) {
        return errorResponse("expiryDate must be an ISO date (yyyy-mm-dd)", 400);
      }

      const { data: item, error } = await admin
        .from("user_pantry")
        .insert({
          user_id: userId,
          ingredient_name: name,
          quantity: body.quantity != null ? Number(body.quantity) : null,
          unit: body.unit ?? null,
          expiry_date: expiryDate,
        })
        .select("pantry_item_id")
        .single();
      if (error) return errorResponse(error.message, 500);

      return json({ pantryItemId: item.pantry_item_id, status: "added" });
    }

    if (req.method === "DELETE") {
      const pantryItemId = new URL(req.url).searchParams.get("pantryItemId");
      if (!pantryItemId) return errorResponse("pantryItemId is required", 400);

      const { error } = await admin
        .from("user_pantry")
        .delete()
        .eq("user_id", userId)
        .eq("pantry_item_id", pantryItemId);
      if (error) return errorResponse(error.message, 500);

      return json({ status: "deleted" });
    }

    return errorResponse("Use GET, POST or DELETE", 405);
  } catch (error) {
    return respondToError(error);
  }
});
