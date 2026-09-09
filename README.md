# Afri Media CRM — Android app

A native Android app for the Afri Media CRM WordPress plugin (`afrimedia-crm`), so Winston can view/manage clients, quotes, and invoices, and record payments from his phone. It talks to a new REST API added to the plugin (`includes/class-amcrm-rest-api.php`, namespace `amcrm/v1`) using your normal WordPress login plus a WordPress **Application Password** — no separate account system to manage.

## 1. Install the updated plugin first

This app needs the CRM plugin at **v1.6.0 or later** (the version that adds the REST API). Update the plugin on your WordPress site the same way as always (replace files via FTP, or delete+reupload — your data is untouched either way), then confirm the update worked:

Visit `https://yoursite.com/wp-json/amcrm/v1/me` in a browser while logged into WordPress in another tab — you should get a `401`/`403` JSON response (that's expected, browsers don't send the app's auth header) rather than a 404. A 404 means the plugin update didn't take.

## 2. Create a WordPress Application Password

This is a built-in WordPress feature (5.6+), separate from your normal login password, and can be revoked any time without changing your real password:

1. In WordPress admin, go to **Users → Profile** (your own profile, or the profile of whichever user the app should sign in as).
2. Scroll to **Application Passwords**.
3. Enter a name like "Android CRM app" and click **Add New Application Password**.
4. Copy the generated password (spaces and all) — WordPress only shows it once.

Your site must be served over **HTTPS** for Application Passwords to work (WordPress disables them over plain HTTP by default) — if `yoursite.com` doesn't have a padlock in the browser, sort that out first (most hosts, including typical shared hosting, offer free HTTPS via Let's Encrypt).

## 3. Get a build of the app

This project couldn't be compiled inside the sandbox that wrote it (no network access to Google's Android SDK/Maven servers there), so pick one of these to get an actual installable APK:

### Option A — Android Studio (simplest, one machine, no accounts needed)

1. Install [Android Studio](https://developer.android.com/studio) (free) on your computer.
2. Open this folder as a project (**File → Open**, pick the `afrimedia-crm-android` folder).
3. Let it sync — the first sync downloads the Android SDK/Gradle dependencies automatically (needs a few hundred MB and a working internet connection, one-time).
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. When it finishes, click the "locate" link in the notification, or find it at `app/build/outputs/apk/debug/app-debug.apk`.
6. Copy that file to your phone (email it to yourself, use a cloud drive, or a USB cable) and open it there to install — Android will prompt you to allow "install unknown apps" for whichever app you opened it from, once.

### Option B — GitHub Actions (no local Android tooling at all)

A workflow is already included at `.github/workflows/build-apk.yml`. If you push this project to a GitHub repository (public or private, free tier is fine):

1. Create a new repo on GitHub and push this folder to it.
2. Go to the repo's **Actions** tab — the "Build APK" workflow runs automatically on push (or trigger it manually via **Run workflow**).
3. When it finishes, open the run and download the `afrimedia-crm-debug-apk` artifact (a zip containing `app-debug.apk`).
4. Transfer that APK to your phone and install it the same way as Option A.

Either option produces the exact same app — Option A is faster if you're comfortable installing Android Studio once; Option B needs nothing installed locally but requires a GitHub account.

## 4. Sign in

Open the app, enter:
- **Site address**: `https://yoursite.com` (no trailing slash needed)
- **WordPress username**: your normal WordPress username
- **Application Password**: the one you generated in step 2 (paste it exactly, spaces included — the field accepts them)

## What the app can do (v1)

- **Dashboard** — invoiced / collected / outstanding for the current month, broken down by branch if you're a full admin with more than one branch.
- **Clients** — search, view, create, edit, delete.
- **Quotes** — list (with status), view, create with line items, edit, convert to an invoice.
- **Invoices** — list, view, create with line items, edit, and record a payment on the spot.
- **Projects & Tasks** — list/create/edit/delete projects (client, hourly rate, dates, custom board-column labels); a project screen with a profitability card (quoted/invoiced/collected/cost/profit), its linked quotes/invoices (read-only), and task lists whose tasks are grouped by status as a tap-to-change-status sectioned list (mobile-appropriate — not a drag-and-drop Kanban board); a task screen with status/priority dropdowns, subtasks, a comment feed, and time tracking (start/stop timer with a live elapsed counter, plus manual time entries).

Whatever your WordPress account can do in the web admin, the app can do the same — a full admin sees everything; a branch-locked agent account only sees their own branch's clients/quotes/invoices/projects, exactly like the web version.

## Signing for real (before wider distribution)

Right now `assembleRelease` is configured to sign with the well-known Android **debug key** so a plain build works immediately with zero setup. That's fine for sideloading on your own phone, but:
- Don't distribute that build outside your own devices — the debug key isn't secret, so anyone with the APK could theoretically re-sign a modified version and have it look "the same" as far as your phone's install flow shows.
- Every future `assembleRelease` build with the same signing config can update over a previous install (same key) without uninstalling first, but only among builds signed with that same key — a real distribution key won't match the debug key.

Before installing this more broadly (a Play Store submission, or handing the APK to the Bulawayo agent), generate your own signing key and switch `app/build.gradle.kts`'s release `signingConfig` to use it:

```
keytool -genkey -v -keystore afrimedia-crm-release.keystore -alias afrimedia -keyalg RSA -keysize 2048 -validity 10000
```

Keep that keystore file and its passwords somewhere safe and backed up — losing it means you can never update the app under the same package again, only publish a new one from scratch.

## Project layout

- `app/src/main/java/com/afrimedia/crm/data/` — network layer (Retrofit service, session/credential storage, response models) and the repository that turns HTTP errors into readable messages.
- `app/src/main/java/com/afrimedia/crm/ui/` — Jetpack Compose screens, one package per feature (login, dashboard, clients, quotes, invoices, projects), plus `ui/common` for shared bits (line-item editor, client picker, status colors).
- `app/src/main/java/com/afrimedia/crm/ui/nav/AppRoot.kt` — bottom navigation + screen routing.

## Known rough edges (v1)

- No offline mode — every screen needs a live connection to your site.
- No push notifications for new invoices/payments (would need a separate notification server; not built here).
- Dates are entered as plain `YYYY-MM-DD` text rather than a date picker — functional, just not as polished as it could be.
- Projects/Tasks: the mobile task board is a grouped-by-status list with a tap-to-change-status menu on each task row, not a draggable Kanban board — a deliberate mobile product decision, not a missing feature.
- Projects/Tasks: assignee is picked by typing a raw WordPress user ID rather than a name-search picker (no "assignee picker" component exists yet in this codebase, unlike `ClientPicker`) — functional but not friendly; a follow-up could add a proper user picker.
- Projects/Tasks: linking/unlinking an *existing* quote or invoice to a project from the app was deferred — the project screen shows already-linked quotes/invoices read-only. Creating a brand-new quote/invoice already lets you attach it to a client from the app; project-linking would need a small "pick an unlinked quote/invoice" screen added later.
- Projects/Tasks: the project list can't show the client's name next to each project (only `client_id`) — `project_payload()` on the backend only returns `client_id` in the list endpoint (the single-project GET is the same), so the list row shows "Client #<id>" rather than firing an extra lookup per row.
- Projects/Tasks: the running-timer elapsed counter is a simple in-memory ticking counter (`LaunchedEffect` + `delay(1000)`) seeded from the timer's `started_at` on load — it doesn't survive process death gracefully, but the server's timer state is always the source of truth on next load, so nothing is lost.
- This project was written but never compiled end-to-end (see the sandbox limitation above) — if Android Studio's first build surfaces a dependency-version hiccup, it's almost always a one-line version bump in `app/build.gradle.kts` that Android Studio's own error message + "Fix" suggestion will resolve directly.
