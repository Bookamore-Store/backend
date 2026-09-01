# Bookamore — Backend

REST API for the Bookamore book marketplace: catalogue, offers, users, images and
authentication. Java 17 + Spring Boot 3.5.3, PostgreSQL 16, schema managed by Liquibase.

Part of a three-repository setup:

| Repository | Contents |
|---|---|
| [`Bookamore-Store/backend`](https://github.com/Bookamore-Store/backend) | this service |
| [`Bookamore-Store/front`](https://github.com/Bookamore-Store/front) | React 19 SPA |
| [`Bookamore-Store/gitops`](https://github.com/Bookamore-Store/gitops) | docker-compose, CI/CD, deployment |

---

## 1. Quick start

### Option A — full stack (recommended)

The local stack lives in `gitops` and builds this service from the sibling checkout, so
clone all three repositories next to each other:

```bash
mkdir -p ~/projects/bookamore && cd ~/projects/bookamore
git clone git@github.com:Bookamore-Store/front.git
git clone git@github.com:Bookamore-Store/backend.git
git clone git@github.com:Bookamore-Store/gitops.git

cd gitops && docker compose -f docker-compose-local.yaml up --build
```

Open **http://localhost:8080** — Nginx fronts everything. Hitting the Vite port directly
breaks `/api/v1/*`, image serving and social login.

### Option B — this service alone

Useful when you only touch the backend. Note the database name: the local profile expects
`bookamore-db`, not `bookamore`.

```bash
docker run -d --name bookamore-db -p 5432:5432 \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=bookamore-db postgres:16-alpine
```

`application-local.yaml` points at the host `bookamore-db` — that is the *compose service
name*, which does not resolve outside Docker. Override the URL when running on the host:

```bash
SPRING_PROFILES_ACTIVE=local \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bookamore-db \
  ./mvnw spring-boot:run
```

The service starts on **:8080**. Liquibase runs automatically at startup.

---

## 2. Configuration

### The `.env` file

`EnvConfig.loadEnv()` reads `src/main/resources/.env` before Spring starts and copies every
entry into system properties. The path works both on the host and inside the image, because
the Dockerfile uses `/app` as `WORKDIR`. The file is git-ignored and missing values are
tolerated (`ignoreIfMissing`), so an incomplete `.env` fails later with a placeholder
resolution error rather than at load time.

Minimum set for `local`:

```properties
JWT_SECRET=<any long random string>
JWT_EXPIRATION=86400000
CLIENT_URL=http://localhost:8080
GOOGLE_CLIENT_ID=<from Google Cloud Console>
GOOGLE_CLIENT_SECRET=
FACEBOOK_CLIENT_ID=
FACEBOOK_CLIENT_SECRET=
```

### Profiles

`spring.profiles.active` defaults to `local`; deployments override it with
`SPRING_PROFILES_ACTIVE`.

| Profile | Datasource | SQL seed | Logging |
|---|---|---|---|
| `local` | hardcoded `bookamore-db:5432/bookamore-db` | `data-local.sql` on every start (`mode: always`) | Hibernate SQL + Security `DEBUG` |
| `dev` | `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | — | Security `DEBUG` |
| `prod` | `SPRING_DATASOURCE_*` supplied by compose | `mode: never` | `INFO`, Security and Hibernate at `WARN` |

`prod` deliberately silences the `DEBUG` logging that `application.yaml` enables globally —
on a 1 GB e2-micro that logging is measurable I/O and log volume.

### `CLIENT_URL` — one variable, two jobs

It is both the CORS allow-list and the OAuth2 redirect target. It may hold a comma-separated
list; the allow-list uses the whole value, while redirects use the **first entry only**, with
trailing slashes stripped. Keep the environment's own origin first, or users will be bounced
to a different host after login.

---

## 3. Database and migrations

PostgreSQL 16. The schema is owned by Liquibase — never edit tables by hand and never rely on
`ddl-auto`, which is `none` in every profile.

```
src/main/resources/db/changelog/
├── master.yaml          # include list, the entry point
└── changes/
    ├── 001_initial.sql
    ├── 002_remove_test_field.sql
    └── 003_create_favorites.sql
```

Migrations run on startup (`spring.liquibase.enabled: true`) and are filtered by context,
which is bound to the active profile.

A new migration is a new numbered file in `changes/` plus an `include` in `master.yaml`.
Never edit a changeset that has already been applied anywhere — Liquibase stores its checksum
and will refuse to start.

`db/scripts/` holds one-off data-repair SQL, and `liquibase_flow/` holds helper scripts for
diffing a live database against the entities. Neither runs automatically.

---

## 4. API

Base path `/api/v1`. Interactive documentation is served by springdoc:

- Swagger UI — `/swagger-ui.html`
- OpenAPI JSON — `/v3/api-docs`

| Controller | Base path | Notes |
|---|---|---|
| `AuthController` | `/api/v1/auth` | `signup`, `signin`, `verify-email` |
| `BookController` | `/api/v1/books` | `GET` is public |
| `OfferController` | `/api/v1/offers` | `GET` is public |
| `UserController` | `/api/v1/user` | authenticated |
| `FavoriteController` | `/api/v1/favorites` | authenticated |
| `ImageController` | `/api/v1/images` | authenticated |

Ready-made requests for manual poking live in `http-requests/` (IntelliJ HTTP client format).

### Public vs authenticated

Everything not listed below requires a valid JWT (`SecurityConfig`):

- `/api/v1/auth/**`, `/oauth2/**`, `/login/oauth2/**`
- `GET /api/v1/books/**` and `GET /api/v1/offers/**`
- `/swagger-ui/**`, `/v3/api-docs/**`
- `/actuator/health`, `/actuator/info`

---

## 5. Authentication

Two paths into the same JWT: email/password via `/api/v1/auth/signin`, and OAuth2 via Google
or Facebook.

The OAuth2 flow is worth understanding before touching it, because it spans three components:

1. The SPA sends the user to `/oauth2/authorization/google` — **on the frontend origin**, so
   Nginx must proxy it here. That is what the `^/(login/)?oauth2/` rule in `front/nginx.conf`
   does.
2. Google calls back to `/login/oauth2/code/google`, Spring's own endpoint. The same Nginx
   rule routes it.
3. `OAuth2SuccessHandler` mints a JWT and redirects the browser to
   `{CLIENT_URL}{OAUTH2_CALLBACK_PATH}?token=…`. `OAuth2FailureHandler` uses the same constant
   with `?error=oauth2_login_failed`.

> **`OAUTH2_CALLBACK_PATH` must match a route the SPA actually serves.** It is currently
> `/login`. Pointing it at `/oauth2/callback` looks tidier but breaks the flow twice over: the
> SPA has no such route, and the Nginx rule above would send the request here instead of to the
> SPA, so Spring answers `No static resource oauth2/callback` with HTTP 500. If the route is
> ever renamed, change it in `front/src/app/router/router.tsx`, in this constant, and add an
> exact-match `location` in `front/nginx.conf` — all three, or not at all.

Redirect URIs to register in the Google Cloud Console OAuth 2.0 client:

| Environment | Redirect URI |
|---|---|
| Local Docker | `http://localhost:8080/login/oauth2/code/google` |
| Dev | `https://bookamore-dev.alt-web.biz.ua/login/oauth2/code/google` |
| Prod | `https://www.bookamore.store/login/oauth2/code/google` |

---

## 6. File uploads

Images are written to `file.upload-dir` (`/app/uploads` in containers), hashed with SHA-256 so
identical files are stored once. Upload subdirectories are created with `775`. Spring caps
uploads at 5 MB per file and per request.

In deployed environments the directory is a Docker volume shared read-only with the frontend
container, which serves it under `/img/`. Uploads therefore survive redeploys — they are not
part of the image or the git checkout.

---

## 7. Build and deployment

```bash
./mvnw test                       # unit tests
./mvnw -DskipTests clean package  # jar into target/
```

| Dockerfile | Used by |
|---|---|
| `Dockerfile` | CI — the image published to GHCR and run in dev and prod |
| `Dockerfile-local` | the local compose stack, with sources mounted for hot reload |
| `Dockerfile-prod` | legacy, not used by the current pipeline |

Deployment is automatic and needs no manual step on any server:

```
merge into dev   →  build.yml → ghcr.io/bookamore-store/bookamore-backend:<sha>  →  DEV
merge into main  →  build.yml → ghcr.io/bookamore-store/bookamore-backend:<sha>  →  PROD
```

`build.yml` pushes `:latest` and `:<sha>`, then fires a `repository_dispatch` at `gitops`,
which pins the exact sha on the target host and restarts the stack. Watch the image build in
this repository's **Actions** tab and the deployment itself in
[gitops → Actions](https://github.com/Bookamore-Store/gitops/actions).

| Environment | URL | Host |
|---|---|---|
| PROD | <https://www.bookamore.store> | GCP e2-micro + Cloudflare Tunnel |
| DEV | <https://bookamore-dev.alt-web.biz.ua> | VPS |

### Memory budget on prod

Prod runs on a 1 GB e2-micro shared with Postgres, Nginx and cloudflared, so the JVM is
started with a deliberately tight set of flags (`-Xmx256m -Xss256k -XX:+UseSerialGC`, serial
GC and tiered compilation stopped at level 1), defined in `docker-compose.gcp.yaml`. Adding a
memory-hungry dependency can push the container into the OOM killer — check
`deploy/gcp/README.md` in `gitops` before doing so.

---

## 8. Project layout

```
src/main/java/com/bookamore/backend/
├── controller/   REST endpoints
├── service/      business logic (interface + impl)
├── repository/   Spring Data JPA
├── entity/       JPA models
├── dto/          request/response objects
├── mapper/       MapStruct entity ↔ DTO
├── config/       security, OpenAPI, uploads, .env loading
├── jwt/          token issuing and parsing
├── handler/      OAuth2 success/failure, global error handling
├── exception/    domain exceptions
├── annotation/   custom validation annotations
└── util/
```

Adding a feature usually walks the same path: `entity` (plus a Liquibase changeset) →
`repository` → `dto` + `mapper` → `service` → `controller`.

---

## 9. Conventions

- **Branches:** `TFB-XXX` — this repository is the backend. `TFF-` belongs to `front`,
  `TFD-` to `gitops`.
- **Commits:** `[TASK_ID] - description`, in English.
- **Pull requests:** open against `dev`; the title matches the main commit of the branch.
  Backend PRs are reviewed by the backend team.

The full convention lives in the `gitops` README.
