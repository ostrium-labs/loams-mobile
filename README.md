# Loams mobile

Native phone apps for [Loams](https://github.com/ostrium-labs/loams): **Loams for Android** (Kotlin, Jetpack Compose, connect-kotlin) and **Loams for iOS** (SwiftUI, connect-swift). Both talk Connect-RPC to a Loams instance, generated from the same protos the server uses. There is no web view, no bridge and no Kotlin Multiplatform.

The design is §37 of the main repository ([`docs/design/37-desktop-and-mobile-apps.md`](https://github.com/ostrium-labs/loams/blob/main/docs/design/37-desktop-and-mobile-apps.md)); the plans are AP2 (Android) and AP3 (iOS).

> **Status: scaffold, mock-first.** Everything runs against the local mock server in [`mock/`](mock/). A real Loams server needs the unified auth plan (pairing grant, DPoP, the Authentik token exchange) and AP4 (the server side of the app protos). Nothing is published to a store yet.

## What the apps do

- **Pair** with a Loams instance by scanning a QR code (pinned TLS key and instance key), or **sign in** through Authentik with OIDC and PKCE.
- **Approve or reject** pending operations. Every decision is signed by a hardware key (Android Keystore or the Secure Enclave) that needs biometrics or the device passcode for each signature.
- **Watch** the instance and its operations live over a Connect server stream.
- **Receive push notifications** that are sealed end to end (HPKE), so Apple, Google and the push gateway see only ciphertext.

## Layout

| Path | What |
|---|---|
| `android/` | Gradle (Kotlin DSL) project: `:core`, `:proto`, `:data`, `:push`, `:app`, `:conformance` |
| `ios/` | XcodeGen `project.yml`, the `Loams` app, Swift packages `LoamsCore`, `LoamsProto`, `LoamsData` |
| `mock/` | A small Connect server in Go that serves the app protos for local testing |
| `proto/` | The protos, vendored from the main repository at the ref in `conformance/proto-ref.lock` |
| `conformance/` | The proto ref lock and golden fixtures shared by both apps and the mock |
| `docs/` | [Running the apps](docs/RUNNING.md), [protos and generation](docs/protos.md) |

## Licence

Apache License 2.0, the same as the main repository. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
