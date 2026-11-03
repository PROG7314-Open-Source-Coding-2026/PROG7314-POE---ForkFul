//Auth refresh function
// POST /functions/v1/auth-refresh
// Request : { refreshToken }
// Response: { token, refreshToken, expiresAt, userId }
// Called with the anon key as the bearer, because the user's access token
// has already expired by the time the app needs a new one.

import { anonClient, errorResponse, handleOptions, json, respondToError } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "POST") return errorResponse("Use POST", 405);

  try {
    const body = await req.json().catch(() => ({}));
    const refreshToken = String(body.refreshToken ?? "").trim();
    if (!refreshToken) return errorResponse("refreshToken is required", 400);

    const supabase = await anonClient();
    const { data, error } = await supabase.auth.refreshSession({ refresh_token: refreshToken });
    if (error || !data.session || !data.user) {
      return errorResponse("Session expired - please sign in again", 401);
    }

    return json({
      token: data.session.access_token,
      refreshToken: data.session.refresh_token,
      expiresAt: data.session.expires_at ?? null,
      userId: data.user.id,
    });
  } catch (error) {
    return respondToError(error);
  }
});
