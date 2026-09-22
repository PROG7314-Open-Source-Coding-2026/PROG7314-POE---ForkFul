// POST /functions/v1/auth-sso
// Exchanges an identity for a server-issued Supabase session JWT.
// Request : { ssoProvider: "google"|"email", idToken?, email?, password?, displayName?, mode? }
// Response: { token, userId, isNewUser }

import { anonClient, errorResponse, handleOptions, json, respondToError, serviceClient } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "POST") return errorResponse("Use POST", 405);

  try {
    const body = await req.json();
    const provider = String(body.ssoProvider ?? "").toLowerCase();
    const supabase = await anonClient();

    let session: { access_token: string; user: { id: string; email?: string } } | null = null;

    if (provider === "google") {
      const idToken = String(body.idToken ?? "");
      if (!idToken) return errorResponse("Missing Google ID token", 400);

      // Verify the token with Google before exchanging it with Supabase auth.
      const googleCheck = await fetch(
        `https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`,
      );
      if (!googleCheck.ok) {
        return errorResponse("Google rejected the ID token", 401);
      }
      const tokenInfo = await googleCheck.json();
      if (!tokenInfo.email || tokenInfo.email_verified === "false") {
        return errorResponse("Google account email is not verified", 401);
      }

      const { data, error } = await supabase.auth.signInWithIdToken({
        provider: "google",
        token: idToken,
      });
      if (error || !data.session) {
        return errorResponse(error?.message ?? "Google sign-in failed", 401);
      }
      session = {
        access_token: data.session.access_token,
        user: { id: data.user.id, email: data.user.email },
      };
    } else if (provider === "email") {
      const email = String(body.email ?? "").trim();
      const password = String(body.password ?? "");
      const mode = String(body.mode ?? "signin");

      const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email);
      if (!emailOk) return errorResponse("Please provide a valid email address", 400);
      if (password.length < 6) return errorResponse("Password must be at least 6 characters", 400);

      if (mode === "signup") {
        const { data, error } = await supabase.auth.signUp({
          email,
          password,
          options: { data: { full_name: body.displayName ?? null } },
        });
        if (error) return errorResponse(error.message, 400);
        if (!data.session) {
          return errorResponse("Check your inbox to confirm your email address, then sign in", 403);
        }
        session = {
          access_token: data.session.access_token,
          user: { id: data.user.id, email: data.user.email },
        };
      } else {
        const { data, error } = await supabase.auth.signInWithPassword({ email, password });
        if (error || !data.session) {
          return errorResponse("Invalid email or password", 401);
        }
        session = {
          access_token: data.session.access_token,
          user: { id: data.user.id, email: data.user.email },
        };
      }
    } else {
      return errorResponse("ssoProvider must be 'google' or 'email'", 400);
    }

    const displayName =
      (body.displayName as string | undefined) ?? session.user.email?.split("@")[0] ?? "Forkful cook";

    const admin = await serviceClient();

    const { data: existing } = await admin
      .from("users")
      .select("user_id")
      .eq("user_id", session.user.id)
      .maybeSingle();
    const isNewUser = !existing;

    const { error: upsertError } = await admin.from("users").upsert(
      {
        user_id: session.user.id,
        email: session.user.email ?? body.email ?? null,
        display_name: displayName,
      },
      { onConflict: "user_id" },
    );
    if (upsertError) {
      return errorResponse(`Could not create the user profile: ${upsertError.message}`, 500);
    }

    return json({
      token: session.access_token,
      userId: session.user.id,
      isNewUser,
    });
  } catch (error) {
    return respondToError(error);
  }
});
