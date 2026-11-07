# Meridian — Watch Management

MSS301 Assignment 02 · HE190091. A watch inventory workspace with a React JavaScript frontend, Bootstrap, and four Spring Boot microservices using an orchestrated Saga.

## Required database

**Microsoft SQL Server 2019**, at `localhost:1433`, with SQL authentication **`sa` / `sa`**. The default application configuration uses these credentials. The three databases are `DBAccount`, `DBBrand`, and `DBWatch`; JPA/Hibernate creates their tables independently. H2 is an explicit test profile only.

The installed local server was confirmed as SQL Server 2019 (15.0.2000.5). Its login currently rejects `sa` / `sa`; the server password was left unchanged at your request. Configure that login on your server, then run:

```powershell
.\scripts\verify-sql.ps1
```

This checks the actual engine major version, authenticates with `sa` / `sa`, and creates missing assignment databases. It does not change server logins. `DB_USERNAME` and `DB_PASSWORD` can override application credentials when explicitly needed.

## Run locally on Windows

Configuration lives in the repository-root **`.env`**. The local file is already populated, including `JAVA_HOME`, SQL Server `sa` / `sa`, database names, service URLs/ports, JWT settings, and a shared internal service key. It is ignored by Git. For a fresh checkout, copy `.env.example` to `.env` and set your local values.

The PowerShell startup and database-check scripts load `.env` automatically; explicit process environment variables take priority. Spring Boot also imports it when applications start from the repository root or a service module. Vite reads this same root file, exposing only variables prefixed `VITE_` to browser code. Keep secrets unprefixed. Use plain, unquoted `KEY=value` entries and forward slashes in paths. If changing a service port, update its corresponding URL too. Restart services after edits; rebuild the frontend for changes to `VITE_` values.

Prerequisites: Java 21, Maven 3.8+, Node.js 22.12+ (Node 24 used for verification), and SQL Server 2019. Put Java/Maven/Node on PATH, or set `JAVA_HOME` for Java.

```powershell
mvn verify
cd frontend
npm ci
cd ..
.\scripts\start-local.ps1
```

Open **http://127.0.0.1:5173**. The frontend calls the API gateway through Vite's `/api` proxy. The startup script checks SQL Server first and refuses to start if credentials or version are wrong. Processes run without extra windows; logs are in `logs/`.

```powershell
.\scripts\stop-local.ps1
```

To run the verified, temporary in-memory demonstration while configuring SQL Server:

```powershell
.\scripts\start-local.ps1 -Profile h2
```

The H2 profile loses changes when stopped. It is not the assignment's SQL Server deployment. The startup/stop scripts manage only their own process IDs and check command lines before stopping anything.

## The frontend

`frontend/` contains **JavaScript and JSX only**: React functional components and hooks, Vite, Bootstrap 5, custom CSS, and Lucide icons. No TypeScript or class components. The design uses an ivory workspace, forest-green navigation, serif page headings, and abstract dial illustrations rather than misleading product photography.

- Public inventory with newest-first ordering, watch details and all assignment fields.
- Search by watch name, brand, or ID; brand, category and stock filters; sorting and pagination.
- Live totals for timepieces, units, stock value, and low-stock models.
- Brand and category directories linked back to filtered inventory.
- CSV export of the currently filtered collection, with spreadsheet-formula escaping.
- Login, per-tab session persistence, expiry handling, and role-aware controls.
- Administrator create/edit forms, server-error display, and confirmed deletion.
- Responsive mobile navigation, keyboard-accessible dialogs, focus restoration, loading, empty, and connection-error states.

Sign in as `adminsys` / `@1` to manage watches. `staff02` and `member01` use `@1` and have view access. `staff01` and `member02` are inactive. Database credentials are distinct from application login credentials.

All watch fields are required. Names must contain 11–255 characters; prices must be at least $100 and fit DECIMAL(10,2); stock must be nonnegative; create/update must use today's server-local release date. Align browser/server timezones. Historical assignment seed data is preserved even where it predates these entry rules.

## Architecture

| Application | Port | Database | Responsibility |
|---|---|---|---|
| React frontend | 5173 | None | User interface; proxies `/api` to the gateway |
| APIGateway_HE190091 | 8080 | None | Public entry point; JWT and Administrator authorization |
| MSAccount_HE190091 | 8081 | DBAccount | Active-account login and JWT issuance |
| MSBrand_HE190091 | 8082 | DBBrand | Brands and Saga watch counters |
| MSWatch_HE190091 | 8083 | DBWatch | Watches, categories, Saga coordination and audit |

Create increments the brand count before committing locally. Updates migrate brand counts when necessary. Delete decrements the brand count before deleting locally. Local failures reverse completed remote steps. Failed compensation is recorded for reconciliation; logs commit independently of watch changes. Brand counters use row locks and watch records use optimistic versioning.

Downstream APIs require a service key supplied by the gateway/Feign client; counter endpoints are blocked at the gateway. Set the same `SERVICE_INTERNAL_KEY` on all services outside local development. Keep downstream ports private. Seed passwords and checked-in JWT/service keys are assignment defaults.

The Saga is synchronous: process crashes and ambiguous timeouts can still require manual reconciliation. There is no durable replay worker or participant idempotency ledger. Do not blindly retry counter mutations. See [the assignment comparison](docs/assignment-comparison.md).

## Verification

```powershell
mvn verify
cd frontend
npm run build
# With all services and the frontend already running:
npx playwright install chromium
npm run test:e2e
```

The browser suite covers public browsing/export, admin CRUD and actual brand counters, inactive/staff accounts, session persistence, mobile layout, and connection recovery. `PLAYWRIGHT_CHROMIUM_EXECUTABLE` optionally selects an existing Chromium binary.

```powershell
npx --yes newman run postman/MSS301_Assignment2_WatchManagement_Saga.postman_collection.json -e postman/MSS301_Assignment2_Environment.postman_environment.json
```

See [verification notes](docs/frontend-verification.md) for what was executed and what remains pending.

## Optional Docker SQL Server 2019

Use only when local port 1433 is free:

```sh
docker compose up -d mssql
docker compose run --rm mssql-init
```

SQL Server containers require a complex bootstrap password. The isolated container initializer creates the databases, disables password complexity for its `sa` login, and sets its final password to `sa`. Application credentials remain `sa` / `sa`. This initializer never targets the Windows server. The Docker path is not runtime-tested here because Docker is unavailable. `scripts/start-all.sh` remains available for Bash users of this optional setup.

Fresh databases receive 5 accounts, 5 brands, 5 categories and 10 watches. Seed initializers skip populated tables; resets of only one database can invalidate IDs and counts. Existing seed values are not overwritten automatically.
