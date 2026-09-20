# TRX Launcher 6 — Redline Edition

A RAM 1500 TRX home-screen launcher for Uconnect 5 (12" display). Two parts live in this repo:

- `app/` — the native Android app (Java), built by GitHub Actions.
- `preview/` — a React web preview of the same UI, for quick visual checks.

## How to get the APK

The APK is built automatically by GitHub Actions whenever code is pushed to `main`.

1. Create a private GitHub repo and push this project to it.
2. Add these secrets under **Settings → Secrets and variables → Actions**:
   - `MAPS_API_KEY` — an Android-restricted Google Maps key for package `com.mdiaz.trxlauncher`. Enable Navigation SDK, Maps SDK for Android, Places API (New), and Routes API. Billing must remain attached.
   - `KEYSTORE_BASE64` — your release keystore, base64-encoded (`base64 -w0 your.keystore`).
   - `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — the keystore credentials.
3. Push to `main`, or run the workflow manually from the **Actions** tab.
4. Download the finished APK from the workflow run's **Artifacts**.

## Clean-install test

Uninstall any previous TRX Launcher before installing a new build so obsolete preferences and cached launcher state cannot survive.

## Architecture

`MainActivity` → `NavigationScreen` / `ObdService` / `MediaAccessService`

Transmission temperature remains unavailable until a verified 2023 RAM TRX PID is configured; the app does not fabricate it.
