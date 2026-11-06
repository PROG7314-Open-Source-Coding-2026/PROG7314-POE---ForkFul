//Toggle or remove a single grocery item

import { errorResponse, handleOptions, isUuid, json, respondToError, requireUser } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "PUT" && req.method !== "DELETE") return errorResponse("Use PUT or DELETE", 405);

  try {
    const { admin, userId } = await requireUser(req);

    const itemId = new URL(req.url).searchParams.get("itemId");
    if (!isUuid(itemId)) return errorResponse("itemId must be a valid id", 400);

    if (req.method === "DELETE") {
      const { error } = await admin
        .from("grocery_items")
        .delete()
        .eq("user_id", userId)
        .eq("item_id", itemId);
      if (error) return errorResponse(error.message, 500);
      return json({ status: "deleted" });
    }

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

//------------------------------End-Of-File------------------------------//
