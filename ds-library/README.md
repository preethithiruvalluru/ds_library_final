# Data Science Digital Library: DS Lab & Study Portal

A study portal for a Data Science course: modules with book-style PDF reading, running notes, lab experiments with
highlighted code, a tools directory, a resource library, a code editor, a student profile and live GitHub repository info.

**Stack:** Java 17, Spring Boot 3.5, Spring Data MongoDB, Maven, MongoDB, plain HTML/CSS/JavaScript with Bootstrap 5.
Libraries loaded from CDNs: PDF.js, StPageFlip, Monaco Editor, highlight.js, Pyodide (Python in the browser).

## Run it on your computer

You need: JDK 17+, Maven, and MongoDB running on `localhost:27017` (or an Atlas address in `MONGODB_URI`).

```powershell
cd ds-library
mvn spring-boot:run
```

Open http://localhost:8085. **There is no login**: everyone who can open the site can add, change and delete content, so keep it on your own computer or a private network (see `DEPLOYMENT.md`).

Prefer Docker? `docker compose up --build` starts the app together with its own MongoDB (see `DEPLOYMENT.md`).

## Settings (environment variables)

| Variable | Needed? | Meaning |
|---|---|---|
| `MONGODB_URI` | for cloud | Database address. Default `mongodb://localhost:27017/dslibrary` |
| `PORT` | no | Default `8085` (hosts like Render set it for you) |
| `GITHUB_TOKEN` | no | Read-only token; raises the GitHub rate limit from 60 to 5000 requests per hour |
| `ANTHROPIC_API_KEY` | for the AI assistant | Turns on the chat assistant in the bottom-right corner. Without it the chat says it is not set up |
| `CHAT_MODEL` | no | Which Claude model answers (default `claude-opus-5-5`; `claude-haiku-5-5` is much cheaper) |
| `CHAT_PER_VISITOR_PER_HOUR`, `CHAT_DAILY_LIMIT` | no | Question limits per visitor per hour (default 30) and for the whole site per day (default 500) |
| `CHAT_COLLEGE_INFO_FILE` | no | Use your own college-information file instead of the built-in one |
| `RUNNER_URL`, `RUNNER_TOKEN` | optional | Connect the Docker code runner (see `../code-runner/README.md`) |

`.env.example` lists them. Real secrets live only in your environment, never in the code.

## What is where

```
ds-library/
├── pom.xml                       Maven dependencies
├── Dockerfile, docker-compose.yml
├── src/main/java/com/mbu/dslibrary/
│   ├── config/                   MongoConfig, DataSeeder (starter tools), SecurityHeadersFilter (browser security headers)
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

There is no login: every endpoint is open to anyone who can reach the site.

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
| Run code | `GET /execute/languages`, `POST /execute` (Java, C, C++ via the code runner) |
| GitHub | `GET /github/repo?url=`, `GET /github/profile-repos` |
| Files | `GET /files/{id}` and `?download=true` |
| Health | `GET /health` |

## Tests

```powershell
cd ds-library
mvn test
```

57 tests cover open access and the browser security headers, form validation, upload checks (PDF and image headers,
size limits, SVG refusal), YouTube link parsing, GitHub link safety, the AI assistant's limits and prompt safety. They never touch your real database or call the AI.

## Code execution in short

- **JavaScript and Python** run inside your own browser in an isolated worker (10 second limit, no network).
- **Java, C and C++** run in a separate service (`../code-runner`) inside locked-down Docker containers.
- **R and SQL** can be written and saved but not run (no sandbox exists for them yet).
- User code is never executed inside the Spring Boot application itself.

## Replace the placeholders

- College logo: `src/main/resources/static/img/mbu-logo.png` was cut from a screenshot, so it is small. Replace it with the official high-resolution logo (a PNG, keeping the same file name) for a sharper result.
- Your details: open **Profile**, then **Edit**. Nothing is pre-filled.
- Your syllabus: log in and use **Modules**, then **Add Module**.

## AI study assistant

A round **Ask AI** button in the bottom-right corner opens a chat window that helps students with subject doubts
(Python, SQL, statistics, machine learning, lab work...) and college questions.

- **Switch it on:** create an API key in the Anthropic Console and set `ANTHROPIC_API_KEY` before starting the app. The key stays on the server and never reaches the browser. Without a key the chat window says the assistant is not set up (it never makes up answers).
- **College questions:** the assistant answers them ONLY from `src/main/resources/chat/college-info.md`. Fill that file with official details (departments, exam rules, contacts, timings), then restart. Anything not written there, it says it does not know.
- **Subject doubts:** it also sees your modules and subjects from the portal, so its help matches your syllabus.
- **Cost control:** there is no login, so each visitor is limited to 30 questions per hour and the whole site to 500 per day (change with `CHAT_PER_VISITOR_PER_HOUR` and `CHAT_DAILY_LIMIT`). Each answer costs a small amount of API credit; a cheaper model can be chosen with `CHAT_MODEL`.
- **Privacy:** what a student types is sent to Anthropic to get an answer. The chat window reminds students not to share passwords or personal details.
