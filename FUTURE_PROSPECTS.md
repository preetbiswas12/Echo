# Echo — Future Prospects

> Ideas, research and plans recorded on 2026-11-11. **No code changes yet** — this document
> only captures what was ideated and verified.

---

## 1. Context

- Echo's homeserver: a **forked Conduit** (GitHub), deployed on **Render** at
  `https://echo-server-a4ae.onrender.com`, backed by **Turso** DB.
- Chat/messaging works. **Calls (voice/video) are not available** today.
- Echo client: fork of Element X Android (`com.echo`, `com.echo.debug` release/debug packages).

---

## 2. How calls work in Echo (verified in the codebase)

Echo ships with **native MatrixRTC calls** (`features/callnative` + Rust RTC backend over the
`matrix-rtc-android` SDK). There is no P2P/mesh mode — both MatrixRTC modes
(`compatibility`, `matrix_2_0`) require an **SFU + JWT endpoint**.

Call flow (media never touches the homeserver):

```
Echo gets an OpenID token from the homeserver (request_token)
  → presents it to a MatrixRTC Authorization Service (lk-jwt-service)
  → that service verifies the token via the homeserver's federation endpoint
    (GET /_matrix/federation/v1/openid/userinfo)
  → mints a LiveKit JWT
  → voice/video flows phone ↔ LiveKit SFU directly.
```

The app discovers **which SFU to use** from the **homeserver's own discovery response**
(nothing is hardcoded client-side). Two equivalent discovery mechanisms exist:

1. `org.matrix.msc4143.rtc_foci` block inside `/.well-known/matrix/client` — what matrix.org uses.
2. MSC4519 endpoint `GET /_matrix/client/unstable/org.matrix.msc4143/rtc/transports`.

---

## 3. Homeserver requirements for calls

Required by Element Call / MatrixRTC (per element-hq/element-call `docs/self_hosting.md`):

| Requirement | Status on modern Conduit (`next`) | Status on our fork (probed 2026-11-11) |
|---|---|---|
| OpenID token issuing — `POST /user/{userId}/openid/request_token` | ✅ present (`openid.rs`) | presumed ✅ (verify fork vintage) |
| OpenID verification — `GET /_matrix/federation/v1/openid/userinfo` | ✅ present (`server_server.rs`) | presumed ✅ (verify fork vintage) |
| `/.well-known/matrix/client` | ✅ present | ❌ **broken — returns placeholder** `https://your-domain-or-render-hostname` |
| **MSC4140** Delayed Events (call keep-alive) | ❌ absent upstream | ❌ absent |
| **MSC4519** `rtc/transports` endpoint | ❌ absent upstream | ❌ `M_UNRECOGNIZED` |
| **MSC3266** Room Summary (only needed for standalone/federation knocking) | ❌ | ❌ `M_UNRECOGNIZED` |
| **MSC4222** sync v2 `state_after` | ❌ | not checked |

Key insight: **MSC4519 is why the app shows no calls** — with no advertised SFU, the client
cannot start one. The broken well-known placeholder would also break call auth, since
lk-jwt-service resolves the client API via `.well-known/matrix/client`.

---

## 4. Policy findings (checked 2026-11-11)

From Element's official MatrixRTC announcement
("Operator, I need an exit from Big Tech calls", element.io/blog):

> "**Your server needs an SFU if you want to start calls.** Users send their audio and video
> to their own server's SFU. **If it doesn't have one, they use another participant's.**
> Because you don't need your own SFU to **join** a call, it's simpler to roll out."
>
> "Also, please make sure you're not paying a service provider that is **free riding on Matrix**."

- `https://livekit-jwt.call.matrix.org` — Matrix.org Foundation's backend, **for matrix.org
  accounts** (verified live: published in matrix.org's well-known `rtc_foci`; `/healthz` → 200).
- `https://livekit-jwt.call.element.io` — Element's auth bridge, alive but **reserved for
  Element Cloud / EMS customers**.
- Pointing our server at either = free-riding → **not a legitimate path**.

---

## 5. Options considered

| Option | Description | Legit? | Cost / needs |
|---|---|---|---|
| **A** | Use a **matrix.org account** for call-heavy chats (free calls, zero setup). Note: a room lives on one homeserver — chat-on-Conduit + calls-on-matrix.org cannot be split for the same room. | ✅ | none |
| **B** | **Join-only via federation**: chat stays on Conduit; a matrix.org user in the room starts the call; Conduit users join through their SFU (protocol-sanctioned "use another participant's SFU"). | ✅ | none; calls only exist when that user starts them |
| **C** | **Home-hosted SFU**: always-on home PC/old laptop + LiveKit + coturn + router UDP port-forward; lk-jwt-service can run on Render free tier. | ✅ | spare hardware; no payment, no card |
| **C'** | Small VPS (Oracle Always-Free ARM needs card verification — **rejected**; or ~$4–5/mo VPS — **rejected for now**) | ✅ | rejected per current constraints |
| **D** | **Ship chat-only for now**; revisit calls later | ✅ | none |
| — | Point `rtc_foci` at matrix.org / element.io for our users' own calls | ❌ | free-riding, explicitly discouraged |

**Constraints noted:** no VPS, no paid hosting, no payment/card verifications (rules out
Oracle/AWS/GCP/Fly free tiers). Rejected by choice, not feasibility.

---

## 6. Future prospect — step plan (when we pick this up)

1. **Fix `/.well-known/matrix/client`** on the Conduit fork (independent of calls):
   `{"m.homeserver":{"base_url":"https://echo-server-a4ae.onrender.com"}}`.
2. **Try Option B** (join-only via federation) to experience real calls with zero infra.
3. If we want our users to **start** calls:
   - implement **MSC4140 delayed events** in the fork (avoid "stuck" calls), then
   - add discovery — either `rtc_foci` in well-known (simplest) or the MSC4519 route — pointing at **our own** lk-jwt-service + LiveKit SFU (Option C: home-hosted, or a VPS later).
4. Optional reliability extras later: MSC4222 (`state_after`), MSC3266 (room summary).
5. Re-check Element's hosted-backend policy periodically — it may evolve.

**Also recorded (done separately, not calls-related):** welcome page background
(`public/bg.jpg`) + Echo logo (`public/echo_light.png`) shipped with the lightest
release APK artifact (`echo-release-apk-arm64`, R8-minified, arm64-only, en-only resources).
