/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Auth SSO edge function managing Single Sign-On and email authentication.
 * Automatically confirms user registrations to eliminate email verification requirements.
 */

import { anonClient, errorResponse, handleOptions, json, respondToError, serviceClient } from "../_shared/forkful.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return handleOptions();
  if (req.method !== "POST") return errorResponse("Use POST", 405);

  try {
    const body = await req.json();
    const provider = String(body.ssoProvider ?? "").toLowerCase();
    const supabase = await anonClient();
    const admin = await serviceClient();

    let session: {
      access_token: string;
      refresh_token: string;
      expires_at: number | null;
      user: { id: string; email?: string };
    } | null = null;

    if (provider === "google") {
      const idToken = String(body.idToken ?? "");
      if (!idToken) return errorResponse("Missing Google ID token", 400);

      //Verify Google token
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
        refresh_token: data.session.refresh_token,
        expires_at: data.session.expires_at ?? null,
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
        // Auto-confirm user email upon registration without email verification barrier
        const { data: createData, error: createError } = await admin.auth.admin.createUser({
          email,
          password,
          email_confirm: true,
          user_metadata: { full_name: body.displayName ?? null },
        });

        if (createError) {
          return errorResponse(createError.message, 400);
        }

        const { data: signInData, error: signInError } = await supabase.auth.signInWithPassword({
          email,
          password,
        });

        if (signInError || !signInData.session) {
          return errorResponse(signInError?.message ?? "Sign-in following registration failed", 401);
        }

        session = {
          access_token: signInData.session.access_token,
          refresh_token: signInData.session.refresh_token,
          expires_at: signInData.session.expires_at ?? null,
          user: { id: signInData.user.id, email: signInData.user.email },
        };
      } else {
        let { data, error } = await supabase.auth.signInWithPassword({ email, password });

        // If legacy account has an unconfirmed email, automatically confirm it and retry
        if (error && error.message.toLowerCase().includes("email not confirmed")) {
          const { data: userList } = await admin.auth.admin.listUsers();
          const existingUser = userList?.users?.find(
            (u) => u.email?.toLowerCase() === email.toLowerCase(),
          );
          if (existingUser) {
            await admin.auth.admin.updateUserById(existingUser.id, { email_confirm: true });
            const retry = await supabase.auth.signInWithPassword({ email, password });
            data = retry.data;
            error = retry.error;
          }
        }

        if (error || !data?.session) {
          return errorResponse(error?.message ?? "Invalid email or password", 401);
        }

        session = {
          access_token: data.session.access_token,
          refresh_token: data.session.refresh_token,
          expires_at: data.session.expires_at ?? null,
          user: { id: data.user.id, email: data.user.email },
        };
      }
    } else {
      return errorResponse("ssoProvider must be 'google' or 'email'", 400);
    }

    const displayName =
      (body.displayName as string | undefined) ?? session.user.email?.split("@")[0] ?? "Forkful cook";

    const { data: existing } = await admin
      .from("users")
      .select("user_id, onboarded")
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
      refreshToken: session.refresh_token,
      expiresAt: session.expires_at,
      userId: session.user.id,
      isNewUser,
      onboarded: existing?.onboarded === true,
    });
  } catch (error) {
    return respondToError(error);
  }
});

//------------------------------End-Of-File------------------------------//
