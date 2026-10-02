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
| `android/` | Gradle (Kotlin DSL) project: `:core`, `:proto`, `:transport`, `:conformance` (JVM) and `:data`, `:push`, `:app` (Android); see [docs/android.md](docs/android.md) |
| `ios/` | XcodeGen `project.yml`, the `Loams` app and its Notification Service Extension, Swift packages `LoamsCore`, `LoamsProto`, `LoamsData`; see [docs/ios.md](docs/ios.md) |
| `mock/` | A small Connect server in Go that serves the app protos for local testing |
| `proto/` | The protos, vendored from the main repository at the ref in `conformance/proto-ref.lock` |
| `conformance/` | The proto ref lock and golden fixtures shared by both apps and the mock |
| `docs/` | [Running the apps](docs/RUNNING.md), [protos and generation](docs/protos.md), [releasing](docs/release.md) |

## Quick start (Android, Linux or Windows)

```sh
cd mock && go run ./cmd/loams-mock          # terminal 1: the mock on 127.0.0.1:8084
cd android && ./gradlew installDebug        # terminal 2: with an emulator running (Windows: .\gradlew.bat)
```

Then tap **Debug: pair with the local mock** in the app. Full steps, including the emulator's screen lock and push: [docs/RUNNING.md](docs/RUNNING.md).

## CI

| Workflow | Runs on | What |
|---|---|---|
| `android` | ubuntu | `./gradlew assembleDebug test lint`; uploads the debug APK |
| `conformance` | ubuntu | the Android network layer against the mock, end to end |
| `ios` | ubuntu, then macOS | LoamsCore on Linux; then package tests, `xcodegen generate` and `xcodebuild build test` on an iOS simulator, unsigned |
| `mock` | ubuntu | vet, race tests, linux/windows/macOS binaries as an artifact |
| `protos` | ubuntu | buf lint, the proto lock, regenerate-and-diff |
| `dco` | ubuntu | every commit is signed off |

No workflow uses a secret. Store releases wait for the owner actions in [docs/release.md](docs/release.md).

## Licence

Apache License 2.0, the same as the main repository. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
