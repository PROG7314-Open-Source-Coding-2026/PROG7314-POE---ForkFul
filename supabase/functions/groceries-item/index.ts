// PUT /functions/v1/groceries-item?itemId=uuid  { isChecked: true }
// Toggles a basket item's purchased state (FR-24).

import { errorResponse, handleOptions, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "PUT") return errorResponse("Use PUT", 405);

  try {
    const { admin, userId } = await requireUser(req);

    const itemId = new URL(req.url).searchParams.get("itemId");
    if (!itemId) return errorResponse("itemId query parameter is required", 400);

    const body = await req.json();
    if (typeof body.isChecked !== "boolean") {
      return errorResponse("isChecked (boolean) is required in the body", 400);
    }

    const { error } = await admin
      .from("grocery_items")
      .update({ is_checked: body.isChecked })
      .eq("user_id", userId)
      .eq("item_id", itemId);
    if (error) return errorResponse(error.message, 500);

    return json({ status: "ok" });
  } catch (error) {
    return respondToError(error);
  }
});
