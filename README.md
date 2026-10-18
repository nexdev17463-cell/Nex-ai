# NEX AI Android Starter

A Kotlin + Jetpack Compose starter featuring a neon cyan/violet interface, animated orb, splash screen, floating glass navigation, Home/Chat/Studio/Settings, and a user-controlled foreground-service demo with a persistent notification.

## Honest feature status
- UI and animation: included.
- Chat: currently demo UI only; no real AI provider is connected.
- Background mode: visible foreground-service sample only. Android may stop work, and this is not an unrestricted always-on agent.
- Cross-app automation: not implemented. It must use Android-approved APIs, permissions, and user confirmation.
- Gemini: use a trusted server backend and store the API key server-side. Never put provider keys into the APK or source code.

## Build
Use Android Studio with JDK 17 and Android SDK 35, or GitHub Actions (`workflow_dispatch`). The workflow uploads `NEX-AI-debug`.

## Push to a new GitHub repository
Create an empty repository named `NEX-AI`, then from this folder:
```bash
git init
git add .
git commit -m "Initial NEX AI Android app"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/NEX-AI.git
git push -u origin main
```
Never paste tokens into chats or commit them.
