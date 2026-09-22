# Forkful - Full Setup Guide (plain text, step by step)

Everything below assumes Windows + PowerShell. Each command can be pasted
directly. Total time: about 30-45 minutes the first time.

================================================================================
STEP 0 - PREREQUISITES
================================================================================

You need, installed once:

  a) Git                -> winget install --id Git.Git -e
  b) GitHub CLI         -> winget install --id GitHub.cli -e
  c) Android Studio     -> winget install --id Google.AndroidStudio -e
     (open it once and let it install the Android SDK)
  d) Supabase CLI       -> scoop bucket add supabase https://github.com/supabase/scoop-bucket.git
                            scoop install supabase
     (if you do not have scoop: winget install Schniz.scoop first, or skip
      the CLI entirely and paste functions in the dashboard - STEP 5 option B)

Check they work:

  git --version
  gh --version
  supabase --version

================================================================================
STEP 1 - GET THE CODE
================================================================================

  cd $HOME\Desktop
  git clone https://github.com/SanteriSuomi/PROG7314-POE---CraveMatch.git forkful
  cd forkful

(If your team already merged the fork into the org repo, clone the org URL
instead: https://github.com/PROG7314-Open-Source-Coding-2026/PROG7314-POE---CraveMatch)

================================================================================
STEP 2 - CREATE THE SUPABASE PROJECT (the database + API host)
================================================================================

  1. Open https://supabase.com/dashboard and sign in (use "Continue with
     GitHub" to keep everything on one account).
  2. Click "New project".
     - Name: forkful-poe
     - Database password: choose one and SAVE it somewhere
     - Region: closest to South Africa (e.g. eu-central / eu-west)
  3. Wait ~2 minutes for provisioning.
  4. When ready, open "Project Settings" (gear icon) -> "API".
     You will copy THREE values in STEP 6:
       - Project URL           (looks like https://abcdxyz.supabase.co)
       - anon public key       (a long JWT)
       - Project reference     (the abcdxyz part of the URL)

================================================================================
STEP 3 - CREATE THE DATABASE TABLES + SEED DATA
================================================================================

  1. In the Supabase dashboard, open "SQL Editor" (database icon -> SQL).
  2. Click "New query".
  3. Open the file supabase/migrations/0001_forkful_init.sql from the repo,
     copy ALL of it, paste into the editor, press "Run".
     -> Success shows "Success. No rows returned".
  4. New query again: copy ALL of supabase/seed.sql, paste, "Run".
     -> This loads 14 recipes with ingredients, aisles and mood tags.
  5. Verify: "Table Editor" -> recipes should show 14 rows.

PowerShell alternative if you install the CLI and link the project (STEP 5):

  supabase db push
  (run from the repo root after `supabase link`)

================================================================================
STEP 4 - GOOGLE SINGLE SIGN-ON (needed for the SSO rubric item)
================================================================================

  1. Open https://console.cloud.google.com
     - Create a project called "forkful-poe" (billing NOT required).
  2. "APIs & Services" -> "OAuth consent screen":
     - User type: External, fill App name "Forkful", your email, Save.
  3. "APIs & Services" -> "Credentials" -> "Create credentials":
     a) "OAuth client ID" -> Application type: "Android application"
        - Package name: com.emeris.forkful
        - SHA-1: get it from Android Studio:
          open the project -> Terminal -> .\gradlew signingReport
          (use the debug SHA-1)
        - Save. Note the generated client ID (not used in code).
     b) "Create credentials" -> "OAuth client ID" -> "Web application"
        - No extra fields needed. Save.
        - Copy the "Client ID" - THIS is the value the app needs.
  4. Go to "Credentials" -> your Web application -> note the Client secret too
     (only Supabase needs the secret, never the app).

================================================================================
STEP 5 - CONNECT SUPABASE AUTH TO GOOGLE + DEPLOY THE EDGE FUNCTIONS
================================================================================

Option A - Dashboard (no CLI):

  1. Supabase dashboard -> "Authentication" -> "Providers" -> "Google":
     - Enable it
     - Client ID:     the WEB client ID from STEP 4.3b
     - Client Secret: the web client secret from STEP 4.4
     - Save
  2. "Authentication" -> "URL Configuration": for the prototype leave
     redirect URLs as default (the app never uses them - it sends the Google
     ID token straight to our auth-sso function).
  3. "Edge Functions" -> "Create a function":
     - Repeat for each of the 10 functions; name them EXACTLY:
         auth-sso
         recipes-deck
         recipe-detail
         swipes
         recipe-box
         pantry
         pantry-match
         groceries
         groceries-item
         user-preferences
     - Delete the template code in the editor, paste the contents of
       supabase/functions/<name>/index.ts, and press Deploy.
     - IMPORTANT: also paste supabase/functions/_shared/forkful.ts the same
       way? NO - the dashboard editor does not support shared folders, so for
       dashboard deploys use Option B instead (recommended).

Option B - Supabase CLI (recommended, deploys the shared module correctly):

  supabase login
  supabase link --project-ref YOUR-PROJECT-REF

  foreach ($f in "auth-sso","recipes-deck","recipe-detail","swipes",
                 "recipe-box","pantry","pantry-match","groceries",
                 "groceries-item","user-preferences") {
      supabase functions deploy $f
  }

  (run from the repo root - it reads supabase/functions/ including _shared)

================================================================================
STEP 6 - POINT THE APP AT YOUR BACKEND
================================================================================

  Open app/build.gradle.kts in Android Studio (or any editor) and replace
  these three placeholders in defaultConfig:

    SUPABASE_URL          -> "https://YOUR-PROJECT-REF.supabase.co"
    SUPABASE_ANON_KEY     -> "YOUR-SUPABASE-ANON-KEY"   (Project Settings -> API)
    GOOGLE_SERVER_CLIENT_ID -> "xxxx.apps.googleusercontent.com"  (STEP 4.3b)

================================================================================
STEP 7 - RUN THE APP ON A PHYSICAL PHONE
================================================================================

  1. Enable Developer options + USB debugging on the phone.
  2. Plug in, accept the debugging prompt.
  3. Android Studio: open the forkful folder, wait for Gradle sync,
     press Run (Shift+F10) with the phone selected.
  4. First run test list:
       - Login screen shows
       - "Continue with Google" opens the account picker -> picks account
       - Onboarding (4 steps) -> Explore feed loads recipes FROM SUPABASE
       - Swipe a stack: right swipe saves + updates the Recipe Box
       - Pantry: add "Tomatoes" with expiry tomorrow -> expiring pill shows
       - Basket: aisle groups appear after right-swipes
       - Settings: toggle dark mode, reset taste profiles, log out
  5. Verify the data round-trip in Supabase dashboard -> Table Editor:
       users / user_taste_profiles / user_swipes / user_pantry /
       grocery_items should all contain your test rows.
     (This is exactly what the marking video must show.)

================================================================================
STEP 8 - MERGE THIS FORK BACK INTO THE TEAM REPO (PowerShell)
================================================================================

If you cloned the personal fork but the team works in the org repo:

  cd forkful
  git remote add team https://github.com/PROG7314-Open-Source-Coding-2026/PROG7314-POE---CraveMatch.git
  git fetch team
  git checkout -b part2-backend-integration
  git push team part2-backend-integration

Then open a Pull Request on GitHub:

  gh pr create --repo PROG7314-Open-Source-Coding-2026/PROG7314-POE---CraveMatch `
      --head YOUR-GITHUB-USERNAME:part2-backend-integration `
      --title "Part 2: REST API, SSO, swipe engine, pantry, basket" `
      --body "Implements the Part 2 prototype per the Planning & Design document."

Day-to-day workflow for the whole team:

  git checkout -b yourname/feature
  ... code ...
  git add .
  git commit -m "Short imperative summary of the change"
  git push origin yourname/feature

Commit small and often - the rubric rewards 50+ meaningful commits.

================================================================================
STEP 9 - GITHUB PAGES DOCUMENTATION SITE (already enabled on the fork)
================================================================================

The docs/index.html site publishes automatically from the main branch:

  https://santerisuomi.github.io/PROG7314-POE---CraveMatch/

If you move to the org repo and want it there too:

  gh api -X POST repos/PROG7314-Open-Source-Coding-2026/PROG7314-POE---CraveMatch/pages `
      -f "source[branch]=main" -f "source[path]=/docs"

================================================================================
STEP 10 - FINAL POE ROADMAP (do NOT do these for Part 2)
================================================================================

  - Biometric lock: androidx.biometric prompt gating Settings (FR-28)
  - Room + WorkManager offline queue for swipes/checks (FR-30, NFR-06)
  - Firebase Cloud Messaging for expiry/match notifications (FR-29):
      1. Create a Firebase project, add an Android app with package
         com.emeris.forkful + the same SHA-1
      2. Download google-services.json into app/
      3. Add the google-services Gradle plugin + FCM dependency
      4. Post device tokens to a new device_tokens table from the app
      5. Edge function sends pushes via FCM legacy HTTP API with a server key
  - Full localisation: values-zu/ and values-af/ string resources (FR-31)
  - Barcode scanning for the Capture Fridge (FR-17, ML Kit)
  - Signed release APK + Play Store screenshots

================================================================================
TROUBLESHOOTING
================================================================================

  "App runs but Explore is empty / network error"
      -> SUPABASE_URL / ANON_KEY placeholders not replaced (STEP 6),
         or functions not deployed (STEP 5), or device offline.

  "Google button does nothing / crashes"
      -> GOOGLE_SERVER_CLIENT_ID still placeholder (STEP 6), or missing
         Web OAuth client (STEP 4.3b), or phone lacks Play Services.

  "Gradle sync fails: failed to find target android-37"
      -> The API 37 platform is on the canary channel. Android Studio
         auto-installs it; on CLI: sdkmanager --channel=3 "platforms;android-37.0"

  "Functions deploy returns 401/403"
      -> run `supabase login` again, and confirm
         `supabase link --project-ref <ref>` used the right ref.

  "403 when calling auth-sso with email signup"
      -> Supabase email confirmation is on by default; either confirm the
         email or disable "Confirm email" in Authentication -> Providers ->
         Email while prototyping.
