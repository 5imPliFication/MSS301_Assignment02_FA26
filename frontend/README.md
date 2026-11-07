# Meridian frontend

React + JavaScript/JSX + hooks + Bootstrap 5. All requests go through the Spring API gateway.

Vite automatically loads the repository-root `.env` (no separate frontend `.env` is needed). `GATEWAY_URL` controls the local API proxy; `FRONTEND_PORT` and `FRONTEND_PREVIEW_PORT` control the frontend servers. Leave `VITE_API_URL` empty for the proxy. Only `VITE_` settings are exposed to the browser; never prefix database passwords or service keys with `VITE_`.

```sh
npm ci
npm run dev
```

Open http://127.0.0.1:5173. Start the backend applications first; the root README includes Windows startup scripts. Vite proxies `/api` to `http://localhost:8080`, so the browser uses the same origin. The production preview also has this proxy.

```sh
npm run build
npm run preview
```

For another hosting arrangement, set `VITE_API_URL` before building or configure a same-origin reverse proxy for `/api`. This variable is a public API origin, never a secret. Static hosting alone does not provide the Java backend.

## Browser tests

With frontend and backend running:

```sh
npx playwright install chromium
npm run test:e2e
```

The suite creates and deletes one uniquely named test watch and verifies brand counts. Run against a test database. Failure artifacts are ignored under `test-results/` and `playwright-report/`; screenshots also go to root `logs/`.

## Structure

- `src/App.jsx`: workspace, navigation, inventory and catalog directories.
- `src/hooks/useCatalog.js`: cancelable API loading and refresh state.
- `src/hooks/useAuth.jsx`: account state, tab session and expiration handling.
- `src/api.js`: API errors, formatting and CSV export.
- `src/components/`: accessible dialogs, watch editor/details and dial illustrations.
- `src/styles.css`: custom visual system layered over Bootstrap.

Framework references: [React hooks](https://react.dev/reference/react/useEffect), [Bootstrap with Vite](https://getbootstrap.com/docs/5.3/getting-started/vite/), [Vite setup](https://vite.dev/guide/).
