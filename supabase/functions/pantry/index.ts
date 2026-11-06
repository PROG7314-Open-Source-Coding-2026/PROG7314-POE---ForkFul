//Pantry management function

import { aisleFor, errorResponse, handleOptions, isUuid, json, respondToError, requireUser } from "../_shared/forkful.ts";

const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;

//Quantity parser: null when absent, NaN when not a number
function parseQuantity(value: unknown): number | null {
  if (value == null || String(value).trim() === "") return null;
  return Number(String(value).replace(",", "."));
}

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
      if (expiryDate && !ISO_DATE.test(expiryDate)) {
        return errorResponse("expiryDate must be an ISO date (yyyy-mm-dd)", 400);
      }
      const quantity = parseQuantity(body.quantity);
      if (Number.isNaN(quantity)) return errorResponse("quantity must be a number", 400);

      const { data: item, error } = await admin
        .from("user_pantry")
        .insert({
          user_id: userId,
          ingredient_name: name,
          quantity,
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
      if (!isUuid(pantryItemId)) return errorResponse("pantryItemId must be a valid id", 400);

      const { error } = await admin
        .from("user_pantry")
        .delete()
        .eq("user_id", userId)
        .eq("pantry_item_id", pantryItemId);
      if (error) return errorResponse(error.message, 500);

      return json({ status: "deleted" });
    }

    //Edit an existing item (only the fields sent are changed)
    if (req.method === "PUT") {
      const pantryItemId = new URL(req.url).searchParams.get("pantryItemId");
      if (!isUuid(pantryItemId)) return errorResponse("pantryItemId must be a valid id", 400);
      const body = await req.json();

      const update: Record<string, unknown> = {};
      if (body.name != null) {
        const name = String(body.name).trim();
        if (!name) return errorResponse("name cannot be empty", 400);
        if (name.length > 80) return errorResponse("name is too long", 400);
        update.ingredient_name = name;
      }
      if (body.quantity !== undefined) {
        const quantity = parseQuantity(body.quantity);
        if (Number.isNaN(quantity)) return errorResponse("quantity must be a number", 400);
        update.quantity = quantity;
      }
      if (body.unit !== undefined) {
        update.unit = body.unit ? String(body.unit).trim().slice(0, 20) : null;
      }
      if (body.expiryDate !== undefined) {
        const expiryDate = body.expiryDate ? String(body.expiryDate) : null;
        if (expiryDate && !ISO_DATE.test(expiryDate)) {
          return errorResponse("expiryDate must be an ISO date (yyyy-mm-dd)", 400);
        }
        update.expiry_date = expiryDate;
      }
      if (Object.keys(update).length === 0) return errorResponse("Nothing to update", 400);

      const { data: updated, error } = await admin
        .from("user_pantry")
        .update(update)
        .eq("user_id", userId)
        .eq("pantry_item_id", pantryItemId)
        .select("pantry_item_id");
      if (error) return errorResponse(error.message, 500);
      if (!updated || updated.length === 0) return errorResponse("Pantry item not found", 404);

      return json({ status: "updated" });
    }

    return errorResponse("Use GET, POST, PUT or DELETE", 405);
  } catch (error) {
    return respondToError(error);
  }
});

//------------------------------End-Of-File------------------------------//
