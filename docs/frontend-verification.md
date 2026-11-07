# Frontend and SQL Server verification

Verified on 2026-11-07.

## Database configuration

- Local SQL Server engine: **Microsoft SQL Server 2019 Developer Edition, 15.0.2000.5**, TCP port 1433. Confirmed by querying `@@VERSION` with Windows authentication.
- Project defaults: username **sa**, password **sa**. Applies independently to DBAccount, DBBrand and DBWatch.
- Those three databases exist on the local server.
- **SQL authentication is not yet verified:** the server rejects `sa` / `sa`. The user chose to configure the login themselves; no server login/password was changed.
- Run `scripts/verify-sql.ps1` after updating the login. The script rejects non-2019 engine versions and checks the required credentials.
- Optional Compose image now uses SQL Server 2019 with an isolated bootstrap step to establish its own `sa` / `sa` login. Docker runtime was not tested.

## Checks executed

| Check | Result |
|---|---|
| Maven `verify`, all four applications | Passed |
| Java regression tests | 15 passed |
| Frontend dependency installation | Passed, pinned dependency lockfile present |
| React/Vite production build | Passed |
| Playwright desktop public browsing, search, details, directories, CSV export | Passed |
| Playwright admin create/edit/delete and actual brand-count synchronization | Passed |
| Inactive account rejection, staff restrictions and tab-session persistence | Passed |
| Mobile viewport overflow, navigation, login focus and Escape dismissal | Passed |
| Unavailable API error and retry recovery | Passed |
| Mobile layout after final responsive adjustment | Passed |
| Git whitespace checks | Passed |

All browser tests used the real four packaged backend services with separate **H2** databases, not mocked success responses. Only the unavailable-network test intercepts a request to exercise the error state. The suite created, updated and deleted its own test watch, checking the real brand endpoints before and after. The backend SQL Server runtime remains pending because of the login failure above.

The full browser suite passed all five scenarios. A subsequent targeted mobile run passed after switching the mobile table to a stacked layout. Desktop, editor, and mobile screenshots were inspected and are available locally in `logs/frontend-desktop.png`, `logs/frontend-editor.png` and `logs/frontend-mobile.png`. Screenshots, build outputs and runtime logs are ignored by Git.

## Start after configuring the login

```powershell
# Java 21 must be on PATH, or JAVA_HOME must point to its installation.
.\scripts\verify-sql.ps1
.\scripts\start-local.ps1
```

Open http://127.0.0.1:5173. Administrator app login: `adminsys` / `@1`.

For the in-memory preview before the SQL login is ready, use `scripts/start-local.ps1 -Profile h2` explicitly. Stop owned frontend/backend processes with `scripts/stop-local.ps1`.
