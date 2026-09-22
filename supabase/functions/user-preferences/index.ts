// GET /functions/v1/user-preferences   (FR-25)
// PUT /functions/v1/user-preferences   (FR-26, FR-27 + onboarding seeding)
// Body: { language?, dietaryTags?, notificationsEnabled?, theme?,
//         onboarded?, biometricLockEnabled?, seedCuisines?, resetTasteProfiles? }

import { errorResponse, handleOptions, json, respondToError, requireUser } from "../_shared/forkful.ts";

const SEED_POINTS = 10;

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();

  try {
    const { admin, userId } = await requireUser(req);

    if (req.method === "GET") {
      const { data: profile, error } = await admin
        .from("users")
        .select("*")
        .eq("user_id", userId)
        .maybeSingle();
      if (error) return errorResponse(error.message, 500);
      if (!profile) return errorResponse("Profile not found", 404);

      return json({
        userId: profile.user_id,
        email: profile.email,
        displayName: profile.display_name,
        language: profile.preferred_language ?? "en",
        dietaryTags: profile.dietary_tags ?? [],
        notificationsEnabled: profile.notifications_enabled ?? true,
        theme: profile.theme ?? "light",
        onboarded: profile.onboarded ?? false,
        biometricLockEnabled: profile.biometric_lock_enabled ?? false,
      });
    }

    if (req.method === "PUT") {
      const body = await req.json();

      const update: Record<string, unknown> = {};
      if (body.language != null) update.preferred_language = String(body.language).slice(0, 5);
      if (Array.isArray(body.dietaryTags)) update.dietary_tags = body.dietaryTags.map(String).slice(0, 12);
      if (body.notificationsEnabled != null) update.notifications_enabled = Boolean(body.notificationsEnabled);
      if (body.theme != null) update.theme = body.theme === "dark" ? "dark" : "light";
      if (body.onboarded != null) update.onboarded = Boolean(body.onboarded);
      if (body.biometricLockEnabled != null) update.biometric_lock_enabled = Boolean(body.biometricLockEnabled);

      if (Object.keys(update).length > 0) {
        const { error } = await admin
          .from("users")
          .update(update)
          .eq("user_id", userId);
        if (error) return errorResponse(error.message, 500);
      }

      // FR-03: each selected cuisine seeds a taste-profile row with a small
      // starting balance. Recipe seed data includes a cuisine identity tag in
      // its own mood, so the balance lifts every recipe of that cuisine.
      if (Array.isArray(body.seedCuisines) && body.seedCuisines.length > 0) {
        for (const rawKey of body.seedCuisines) {
          const key = String(rawKey).toLowerCase();
          const { data: existing } = await admin
            .from("user_taste_profiles")
            .select("profile_id, points")
            .eq("user_id", userId)
            .eq("mood_profile_key", key)
            .eq("tag_name", key)
            .maybeSingle();

          if (existing) {
            await admin
              .from("user_taste_profiles")
              .update({ points: existing.points + SEED_POINTS })
              .eq("profile_id", existing.profile_id);
          } else {
            await admin.from("user_taste_profiles").insert({
              user_id: userId,
              mood_profile_key: key,
              tag_name: key,
              points: SEED_POINTS,
            });
          }
        }
      }

      // FR-26: reset zeroes every tag point for the user.
      if (body.resetTasteProfiles === true) {
        const { error } = await admin
          .from("user_taste_profiles")
          .update({ points: 0 })
          .eq("user_id", userId);
        if (error) return errorResponse(error.message, 500);
      }

      return json({ status: "ok", updated: Object.keys(update) });
    }

    return errorResponse("Use GET or PUT", 405);
  } catch (error) {
    return respondToError(error);
  }
});
