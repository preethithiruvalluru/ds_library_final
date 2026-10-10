# Data Science Digital Library: DS Lab & Study Portal

A study portal for a Data Science course: modules with book-style PDF reading, running notes, lab experiments with
highlighted code, a tools directory, a resource library, a code editor, a student profile and live GitHub repository info.

**Stack:** Java 17, Spring Boot 3.5, Spring Data MongoDB, Maven, MongoDB, plain HTML/CSS/JavaScript with Bootstrap 5.
Libraries loaded from CDNs: PDF.js, StPageFlip, Monaco Editor, highlight.js, Pyodide (Python in the browser).

## Run it on your computer

You need: JDK 17+, Maven, and MongoDB running on `localhost:27017` (or an Atlas address in `MONGODB_URI`).

```powershell
$env:ADMIN_PASSWORD = "choose-a-long-password"
cd ds-library
mvn spring-boot:run
```

Open http://localhost:8085. Anyone can read. To add, edit or delete, click **Admin login** (username `admin`).
If you do not set `ADMIN_PASSWORD`, a random temporary password is printed in the console when the app starts.

Prefer Docker? `docker compose up --build` starts the app together with its own MongoDB (see `DEPLOYMENT.md`).

## Settings (environment variables)

| Variable | Needed? | Meaning |
|---|---|---|
| `MONGODB_URI` | for cloud | Database address. Default `mongodb://localhost:27017/dslibrary` |
| `ADMIN_PASSWORD` | **yes** | Admin login password (use 10+ characters). Never commit it |
| `ADMIN_USERNAME` | no | Default `admin` |
| `PORT` | no | Default `8085` (hosts like Render set it for you) |
| `GITHUB_TOKEN` | no | Read-only token; raises the GitHub rate limit from 60 to 5000 requests per hour |
| `COOKIE_SECURE` | on HTTPS | Set `true` once the site is served over HTTPS |
| `FORWARD_HEADERS` | behind a proxy | Set `native` on Render, nginx and similar, so each visitor is counted separately |
| `RUNNER_URL`, `RUNNER_TOKEN` | optional | Connect the Docker code runner (see `../code-runner/README.md`) |

`.env.example` lists them. Real secrets live only in your environment, never in the code.

## What is where

```
ds-library/
├── pom.xml                       Maven dependencies
├── Dockerfile, docker-compose.yml
├── src/main/java/com/mbu/dslibrary/
│   ├── config/                   SecurityConfig (login, CSRF, headers), MongoConfig, DataSeeder (starter tools)
│   ├── model/                    MongoDB documents: Module, Note, Experiment, Tool, Resource, StudentProfile, SavedCode
│   ├── dto/                      Request objects with validation rules
│   ├── repository/               Spring Data interfaces
│   ├── service/                  Business logic, FileStorageService (GridFS), GitHubService, CodeRunnerClient
│   ├── controller/               REST endpoints under /api
│   └── exception/                One place that turns errors into clean JSON
├── src/main/resources/
│   ├── application.properties    Reads settings from environment variables
│   └── static/                   The website: index.html, profile.html, css/, js/ (one file per section), img/
└── src/test/java/                Automated tests
```

Uploaded PDFs, photos and images are stored in MongoDB GridFS, so they survive restarts and redeploys.

## Main API (all under `/api`)

Reading (GET) is public. POST, PUT and DELETE need the admin login and a CSRF token (the website handles both).

| Area | Endpoints |
|---|---|
| Dashboard | `GET /dashboard` |
| Modules | `/modules`, `/modules/{id}`, `/modules/{id}/syllabus`, `/modules/{id}/materials` |
| Notes | `/notes?q=&moduleId=&subject=&tag=`, `/notes/subjects`, `/notes/tags` |
| Experiments | `/experiments?q=&moduleId=&language=`, `/experiments/{id}/output-image` |
| Tools | `/tools?q=&category=` |
| Resources | `/resources?q=&type=&subject=&moduleId=&tag=`, `/resources/{id}/file` |
| Profile | `/profile`, `/profile/photo`, `/profile/resume` |
| Saved code | `/code` |
| Run code | `GET /execute/languages`, `POST /execute` (Java, C, C++ via the code runner; admin only) |
| GitHub | `GET /github/repo?url=`, `GET /github/profile-repos` |
| Files | `GET /files/{id}` and `?download=true` |
| Auth | `POST /auth/login`, `POST /auth/logout`, `GET /auth/me` |
| Health | `GET /health` |

## Tests

```powershell
cd ds-library
mvn test
```

43 tests cover the login and CSRF rules, the login attempt limit, form validation, upload checks (PDF and image headers,
size limits, SVG refusal), YouTube link parsing and GitHub link safety. They never touch your real database.

## Code execution in short

- **JavaScript and Python** run inside your own browser in an isolated worker (10 second limit, no network).
- **Java, C and C++** run in a separate service (`../code-runner`) inside locked-down Docker containers. Admin login required.
- **R and SQL** can be written and saved but not run (no sandbox exists for them yet).
- User code is never executed inside the Spring Boot application itself.

## Replace the placeholders

- College logo: replace `src/main/resources/static/img/logo-placeholder.svg` (keep the file name, or edit the two `<img>` tags in `index.html` and `profile.html`).
- Your details: log in as admin and open **Profile**, then **Edit**. Nothing is pre-filled.
- Your syllabus: log in and use **Modules**, then **Add Module**.
