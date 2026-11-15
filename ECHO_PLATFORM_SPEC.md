# Echo Platform Spec

> **Purpose:** this document is the single source of truth for what Echo is, how the
> Android client has been modified from its Element X upstream, how it is tuned to the
> `echo-server` homeserver, and the fundamentals that **every new Echo client
> (desktop, web, iOS) must implement and preserve**.
>
> If you are starting a new Echo client, read section 5 first — it is the contract.

---

## 1. System overview

```
┌──────────────┐   Matrix (E2EE)   ┌─────────────────────────────────────┐
│ Echo clients │ ◄───────────────► │  echo-server (Conduit-next fork)    │
│ android/web/ │                   │  https://echo-server-a4ae.onrender  │
│ desktop/ios  │                   │  Storage: Turso (libSQL) cloud DB   │
└──────┬───────┘                   └──────────────────┬──────────────────┘
       │  push                                        │  push gateway
       │  ▼                                           │  ▼
│  ntfy.sh topics  │ ◄──────────────────────────────┘  (HTTP POST to the
│  (UnifiedPush)   │                                    pushkey = ntfy URL)
└──────────────────┘
```

- **Homeserver URL:** `https://echo-server-a4ae.onrender.com`
- **User IDs:** `@<localpart>:echo-server-a4ae.onrender.com`
- **Push transport:** [ntfy](https://ntfy.sh) topics over UnifiedPush (no FCM anywhere)
- The homeserver is deployed on **Render (free tier)** — it sleeps after inactivity
  and takes tens of seconds to wake. Clients must tolerate slow first requests.

---

## 2. echo-server profile (Conduit-next fork)

A fork of [Conduit-next](https://conduit.rs) (Rust homeserver) with a Turso/libSQL
KV storage backend, deployed via Docker on Render.

### 2.1 Capabilities (as advertised by `GET /_matrix/client/versions`)

| Capability | State |
| :--- | :--- |
| Matrix spec versions | `v1.1` … `v1.12` (authenticated media via v1.11, MSC3916 `stable`) |
| Cross-signing E2EE | ✅ `org.matrix.e2e_cross_signing` |
| Key backup (`/room_keys/*`) | ✅ implemented (`conduit/src/api/client_server/backup.rs`) |
| Sliding sync | ✅ **native simplified** `org.matrix.simplified_msc3575` (no external sync proxy needed) |
| Password login | ✅ `m.login.password` (+ appservice flow) — **no SSO / no OIDC** |
| Registration | ✅ password + **`m.login.registration_token` UIAA stage** (mandatory) |
| MSC2965 OIDC auth metadata (`/_matrix/client/unstable/org.matrix.msc2965/auth_metadata`) | ❌ 404 — clients must fall back to password UI |
| `GET /.well-known/matrix/client` | ✅ served (returns this server as `m.homeserver.base_url`) |
| `GET /.well-known/element/element.json` | ❌ 404 — optional, clients must not hard-fail |
| `GET /_matrix/client/v3/register` | ❌ 405 — registration is POST-only |
| Rate limiting | ✅ client rate limits raised to *effectively unlimited* (implement M_LIMITED handling anyway) |

### 2.2 Auth & registration contract

1. **Registration** (the only way in — every account needs the registration code):

   ```
   POST /_matrix/client/v3/register
   {
     "username": "<localpart>",              // lowercased, strict-validated
     "password": "<password>",
     "initial_device_display_name": "<Echo Android|Echo Desktop|Echo Web|Echo iOS>",
     "auth": { "type": "m.login.registration_token", "token": "<REGISTRATION CODE>" }
   }
   → 200 { "user_id", "access_token", "device_id" }
   ```

   - A single POST **with `auth` inline** is accepted by echo-server (this is what
     Echo Android does). Spec-compliant clients may instead do the UIAA dance:
     first POST without `auth` → `401 { flows: [{stages:["m.login.registration_token"]}], session }`
     → re-POST with `auth` (+ `session`).
   - `registration_token` is set via the `CONDUIT_REGISTRATION_TOKEN` env var on
     Render. Rotating it: change the env var, restart the service. Empty values are
     rejected at startup.
   - **First registered real user is auto-granted server admin** (server-side logic
     in `register_route`). The `@conduit` bot user owns the admin room.
   - Usernames: lowercased client-side, validated with Matrix strict rules,
     duplicates return `M_USER_IN_USE`.

2. **Login:** `m.login.password` only (no SSO, no OIDC, no QR-login against this
   server — the client still supports QR login for *other* servers).

3. **Device display names:** Echo Android uses `"Echo Android"`. Use
   `Echo <Platform>` on every new client so sessions are identifiable in room
   details.

### 2.3 Retention (important UX contract)

- **Messages older than 60 days are redacted server-side** (background checker in
  `timeline/mod.rs`, `message_retention` config key).
- **Media is also subject to 60-day retention**, with a scoped policy system
  (thumbnail / local / remote scopes, created/accessed-based expiry, and a global
  space cap) — see `MediaRetentionConfig` in `conduit-config/src/lib.rs`.
- **Client implications (all platforms):**
  - Old conversations show redacted events; treat this as normal.
  - Old media returns `M_NOT_FOUND` — show a "media no longer available" state.
  - Local caches/stores must be fully self-sufficient; never assume the server
    keeps history or files indefinitely.

### 2.4 Push architecture

- **No FCM.** The Firebase plugin is removed from the client and there is no
  sygnal gateway for `com.echo`.
- Push works through **UnifiedPush with ntfy topics on `https://ntfy.sh`**:
  1. Client registers an HTTP pusher: `PUT /pushers/set` with
     `app_id = "com.echo.<variant>"`, `pushkey = https://ntfy.sh/<topic>`
     (the endpoint URL itself acts as the gateway), standard `data`.
  2. The server POSTs push-gateway notifications (`/send_event_notification`
     format) to the pushkey URL.
  3. The client-side distributor subscribes to the topic and delivers the bytes
     to the app, which then syncs (`FetchPushForegroundService`:
     *"Syncing notifications…"* short service, 3-minute wake-lock window).
  4. Echo Android additionally keeps a **WebSocket subscription**
     (`wss://ntfy.sh/<topic>/ws`, ntfy JSON stream) inside a permanent foreground
     service — *"App is Running"* (`NtfyPushService`) — so pushes arrive without
     Google infrastructure. Other platforms should mirror this pattern (or ship
     the ntfy app as an external distributor on Android).

### 2.5 Deployment facts

- **Render free tier:** the service sleeps when idle; first request after sleep
  can take 30–60 s. Clients should show progress and use generous timeouts
  (Echo Android uses 30 s connect / 60 s read on registration).
- **Storage:** Turso (`libsql://echo-matrix-preetb…`), KV-table schema mirroring
  Conduit trees; chunked + encrypted media stored in the same DB.
- **Turso resilience hardening in this fork:** warm connection pool, single shared
  DB handle with sync watchers, write-lock held only per pipeline request, retry
  on lock contention, CAS-increment back-off, device-token consistency hardening.
- **Config:** secrets live in Render env vars only (`CONDUIT_SERVER_NAME`,
  `CONDUIT_TURSO_URL/AUTH_TOKEN`, `CONDUIT_REGISTRATION_TOKEN`,
  `CONDUIT_ALLOW_REGISTRATION`, `CONDUIT_LOG`, `PORT` → mapped by the image
  entrypoint). Never commit real config.
- **Logging tip:** default `CONDUIT_LOG=warn` hides request paths from error
  lines. Set `CONDUIT_LOG=info` to make trace spans (and the failing `path=`)
  appear on 500s — essential for debugging.

### 2.6 Calls (optional)

TURN is supported by config (`[global.turn]` in `conduit.toml` /
`CONDUIT_TURN_*` env). Not yet verified/configured on the deployment — configure
before promising reliable calls on restrictive NATs.

---

## 3. Echo Android profile (current reference client)

Fork of **Element X Android**, rebranded to **Echo**. All `io.element.android.*`
namespaces were migrated to `com.echo.*`.

| Property | Value |
| :--- | :--- |
| Application ID | `com.echo` (debug: `.debug`, nightly: `.nightly`) |
| Display name | `Echo` (`Echo dbg`, `Echo nightly` in other build types) |
| versionName / versionCode | `1.0.0` / base `20261001`, CalVer-like scheme in `plugins/src/main/kotlin/Versions.kt`; per-ABI builds append `×10 + abiCode` (current artifacts report `202610010`) |
| minSdk / target / compile | 24 / 37 / 37 |
| Build flavor used for releases | `generic` (`assembleGenericRelease`) |
| Architecture | Appyx (navigation) + Molecule (presenters) + Metro (DI) + matrix-rust-sdk |
| Modules | `features/<name>/{api,impl,test}`, `libraries/<name>`, `appconfig`, `appicon/{element,enterprise}` |
| Analytics/telemetry | **disabled** (no PostHog/Sentry keys configured) |

### 3.1 Client-side deviations from upstream (fundamentals)

1. **Password-based account creation restored** (`CreateAccountScreen` in
   `features/login/impl/screens/createaccount`): username + password +
   confirmed-password + **registration code** field (shown when the server
   advertises the `m.login.registration_token` stage). It performs a plain
   `POST /register` over `HttpURLConnection` (30 s connect / 60 s read timeouts)
   because the Rust SDK has no password-registration API, then hands the
   credentials to the SDK to build the session.
   ➜ New platforms without SDK registration support should do the same.
2. **Registration code UX:** the field is only relevant for echo-server; if the
   server later drops the token stage the field can stay blank
   (`takeIf { it.isNotBlank() }`).
3. **Push without Google:** `PUSH_CONFIG_INCLUDE_FIREBASE = false`,
   `PUSH_CONFIG_INCLUDE_UNIFIED_PUSH = true` (`appconfig` /
   `plugins/src/main/kotlin/config/BuildTimeConfig.kt`); embedded ntfy
   distributor lives in `libraries/pushproviders/unifiedpush`.
4. **Branding:** `public/echo_app_logo.png` is the single source logo — launcher
   icon (all densities, adaptive layers) and every notification small icon are
   generated from it (see §6).
5. **Strings/localisation:** en-GB default shared via Localazy; new strings go to
   `temporary.xml` with the naming rules in `AGENTS.md` (`action_`, `common_`,
   `a11y_`, `screen_<name>_<key>`, `error_`, `%1$s` placeholders).
6. **CI:** `.github/workflows/release.yml` builds **separate per-ABI APKs**
   (arm64-v8a, x86_64) via
   `./gradlew :app:assembleGenericRelease -Pci.disableAbiSplits=true -Pci.abiFilters=<abi> -Pci.locales=en`
   on JDK 21 (temurin); artifacts named `echo-release-apk-<abi>`. Only 5
   workflows are kept (`release`, `build`, `build-debug-apk`, `quality`,
   `tests`) — the 19 upstream-only ones were removed.

---

## 4. Brand asset spec (apply to every platform)

Source of truth: `public/echo_app_logo.png` — 395×389 RGBA. Structure: an opaque
**white square with curved corners** carrying a **navy "E" mark** with a **white
4-pointed spark** inside; only the corners are transparent.

| Token | Value |
| :--- | :--- |
| Echo Navy (mark) | `#071A6E` (measured dominant `(7, 26, 110)`) |
| Echo White | `#FFFFFF` |
| Spark | white, cut *out* of glyph derivatives |

Derivation rules (use the same on all platforms):

- **Launcher / app icon:** white background + the navy mark scaled to ~62 % of
  the canvas, centred inside the adaptive-icon safe zone (66/108 dp). Monochrome
  (themed) layer = white silhouette of the mark with the spark transparent.
- **Notification small icons:** **white-on-transparent glyph only** — mark ≈ 66 %
  of a 24 dp canvas, spark transparent, no square, no colour. The system tints
  and backgrounds it. Never ship a coloured notification icon.
- **Legacy (pre-Android-8) launcher icon:** opaque white square + mark at 78 %.
- Generate all densities from one script pass; keep the spark hole transparent.

---

## 5. Fundamentals every Echo client must keep (the contract)

1. **Password + registration-code auth** against echo-server; no OIDC, no SSO.
   Use `initial_device_display_name = "Echo <Platform>"`.
2. **Tolerate Render cold starts:** show progress, use ≥30 s connect / ≥60 s read
   timeouts, retry idempotent setup calls with back-off.
3. **Never hang the UI on post-auth setup.** Lesson learned from Android: after
   registration/login, bound every wait (e.g. verification-state wait is capped
   at 10 s in Echo Android) and let sync/retries continue in the background. A
   burst of server `500`s must degrade to a retryable error, never a frozen
   screen.
4. **Sliding sync** via the server's native simplified MSC3575 support — no
   external proxy. If your SDK can't do simplified sliding sync, fall back to
   `/sync` v2 (Conduit supports both).
5. **E2EE everywhere:** cross-signing, key backup (`/room_keys/*` implemented),
   device verification. Register pushers and set account data like the reference
   client does.
6. **UnifiedPush/ntfy push contract:** pusher `app_id = "com.echo.<variant>"`,
   pushkey = ntfy topic URL, white-glyph notification icons, "App is Running"
   style foreground subscription where the platform allows it.
7. **Retention-aware UX:** 60-day message + media retention is expected server
   behaviour — redactions and dead media links are normal, not bugs.
8. **Rate-limit hygiene:** server limits are effectively unlimited today, but
   always implement `M_LIMITED` / `retry_after_ms` handling so a future
   lockdown or another homeserver still works.
9. **Well-known tolerance:** `/.well-known/matrix/client` works, but
   `element.json` and MSC2965 metadata do **not** exist — degrade gracefully.
10. **Branding:** Echo Navy `#071A6E` + white, logo from `public/echo_app_logo.png`,
    notification glyph rules from §4, app name exactly `Echo`.
11. **Registration code distribution:** the code is the operator's shared secret;
    treat it as confidential in UI (password-style field) and allow rotation
    without breaking existing sessions.

---

## 6. Where things live (reference map)

| What | Path |
| :--- | :--- |
| Brand logo source | `public/echo_app_logo.png` |
| Adaptive launcher icon XML | `appicon/element/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` |
| Launcher foreground/monochrome layers | `appicon/element/src/main/res/mipmap-xxxhdpi/ic_launcher_{foreground,monochrome}.png` |
| Adaptive background colour | `appicon/element/src/main/res/values/echo_app_icon.xml` (`echo_app_icon_background`) |
| Legacy launcher PNGs | `appicon/element/src/main/res/mipmap-{mdpi…xxxhdpi}/ic_launcher.png` |
| Shared notification small icon | `libraries/designsystem/src/main/res/drawable-{mdpi…xxxhdpi}/ic_notification.png` |
| Distributor notification icon | `libraries/pushproviders/unifiedpush/src/main/res/drawable-{mdpi…xxxhdpi}/ic_echo_push_notification.png` |
| Embedded ntfy distributor | `libraries/pushproviders/unifiedpush/.../distributor/{NtfyPushService,NtfyDistributorReceiver,DistributorConfig}.kt` |
| Push fetch foreground service | `libraries/push/impl/.../push/FetchPushForegroundService.kt` |
| Account creation (registration code) | `features/login/impl/.../screens/createaccount/CreateAccount{Presenter,View}.kt` |
| Registration HTTP call | `libraries/matrix/impl/.../auth/RustMatrixAuthenticationService.kt` (`createAccount`) |
| App identity / push flags | `plugins/src/main/kotlin/config/BuildTimeConfig.kt` |
| Version scheme | `plugins/src/main/kotlin/Versions.kt` |
| Release pipeline | `.github/workflows/release.yml` |
| Server: registration | `echo-server/conduit/src/api/client_server/account.rs` (`register_route`) |
| Server: key backup | `echo-server/conduit/src/api/client_server/backup.rs` |
| Server: media retention | `echo-server/conduit/src/database/key_value/media.rs` |
| Server: message retention | `echo-server/conduit/src/service/rooms/timeline/mod.rs` (`start_message_retention_checker`) |
| Server: push delivery | `echo-server/conduit/src/service/pusher/mod.rs` |

---

## 7. Changelog (Echo-specific history)

### Echo Android client

| Commit | Change |
| :--- | :--- |
| `c84a56c2` / `ad063dcb` | ktlintFormat sweeps after the rebrand |
| `582ea791` | Remove unused google-services plugin, fix fork CI jobs |
| `cae59315` | Login tests switched to the Echo default homeserver |
| `ec786571` | Echo background image |
| `ea891a93` | Konsist/detekt/lint adjustments for the rebrand |
| `7bb75fc9` | Skip Paparazzi screenshot tests in the Test workflow |
| `89029b9c` | ntfy distributor: `runCatchingExceptions` |
| `5494ff25` | **Restore password-based account creation** (CreateAccount screen + registration code) |
| `8deadc6c` | Rename the push distributor foreground notification ("App is Running") |
| `1e3f7d55` | Use `echo_app_logo.png` as the launcher icon; rename the app to Echo in docs |
| `a6f27b8e` | Remove 19 upstream-only CI workflows; Echo docs |
| `89630fb7` / `543522c8` | Adaptive icon + manifest roundIcon + IconPreview fixes |
| `afaa4c23` / `cf0e9554` | Push notification icon → white logo PNG (duplicate resource resolved) |
| `fb6f5037` | Proper adaptive icon layers (white bg colour + safe-zone foreground + monochrome), legacy icons, density-bucketed push glyph |
| `f54bc2ff` | Shared `ic_notification` (all message notifications, calls, fetch service) → Echo glyph |

### echo-server

| Commit | Change |
| :--- | :--- |
| `1c161a2` | Rate-limit config fix for fork schema; cfg fallback for backend features |
| `cb3b95c` | Chunked, encrypted Turso media backend |
| `63e008e` | Client rate limits → effectively unlimited |
| `0d1ef67` | **60-day message and media retention** |
| `20a84bc` | Device-token handling hardened against inconsistent DB state |
| `eb3a370` / `de89fdd` / `5115838` | Turso write retry / lock scoping / CAS back-off |
| `59ece37` / `e054c28` | Shared DB handle + sync watchers; warm connection pool |
| `8a972e3` | Gitignore local AI session logs (`.opencode/`) |

---

## 8. Open items / known issues

1. **Registration burst `500`s + UI freeze:** during account creation the server
   returned 5 rapid 500s (9–11 ms each) on follow-up requests; the Android UI
   hung and only completed after an app restart (the account itself was created).
   Under investigation — reproduce with `CONDUIT_LOG=info` and/or a controlled
   registration probe. Fix belongs in the server; the client-side rule (§5.3)
   already covers the UX.
2. **ntfy.sh dependency:** push currently rides the public ntfy service. Consider
   self-hosting ntfy later; the pusher/endpoint contract stays identical.
3. **Retention UX:** nothing in the UI explains that messages/media vanish after
   60 days — surface it on all platforms.
4. **TURN for calls** is supported in config but not verified on the deployment.
5. **Render free tier cold starts** make the first login after idle slow; a
   keep-alive ping (e.g. uptime monitor) is an option if it becomes a problem.
