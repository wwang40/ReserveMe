# Frontend (Vite)

This frontend has been migrated to Vite + React + TypeScript.

Quick start (requires Node.js >= 18):

1. cd frontend
2. npm install
3. npm run dev

Build and preview:

- npm run build
- npm run preview

Notes:
- `src/services/api.ts` uses `/` as the base URL; for local development set `proxy` in `vite.config.ts` or update the axios baseURL to `http://localhost:8080` if your backend runs there.
