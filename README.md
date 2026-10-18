# NEX AI — Android + secure backend starter

A Kotlin/Jetpack Compose Android client with neon red/pink styling and a small Node.js backend that routes chat requests to Gemini or OpenAI. OpenAI image generation is provided by the backend endpoint. This is a starter: voice capture, PDF/document upload/analysis, persistent conversation history, account/login, and production-grade authentication are not yet implemented.

## Security

- Never put provider API keys in Android code, Gradle config, GitHub commits, or screenshots.
- Deploy `backend/` to a trusted HTTPS host and add `GEMINI_API_KEY` and `OPENAI_API_KEY` as that host's server-side secrets.
- The backend currently has basic request rate limiting, but no user authentication. Do not expose it publicly for real use until authentication/abuse controls are added.

## Build APK

GitHub Actions builds a debug APK when you push to `main` or run the workflow manually. Download the `NEX-AI-debug` artifact, extract it, and install `app-debug.apk`.

## Connect the app

1. Deploy `backend/` using its Dockerfile or Node.js start command `npm start`.
2. Set server environment secrets `GEMINI_API_KEY` and `OPENAI_API_KEY`. Never add them to the Android project.
3. Verify `https://YOUR-HOST/health` returns JSON with `ok: true`.
4. Open NEX AI → Settings → AI Connection and paste the backend's HTTPS base URL (no trailing slash).
5. In Chat, select Gemini or OpenAI. Chat requests go to `/api/chat`.

## API endpoints

- `GET /health`
- `POST /api/chat` JSON: `{ "message": "Hello", "provider": "gemini" }` or provider `openai`; response `{ "provider": "...", "reply": "..." }`.
- `POST /api/image` JSON: `{ "prompt": "A neon city" }`; response contains `imageBase64` and `mimeType`.

Image generation endpoint is backend-ready but the Android Studio tab is still a visual placeholder. Voice and file analysis are planned next, not yet functional.
