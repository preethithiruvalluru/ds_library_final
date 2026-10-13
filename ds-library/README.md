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
| `CHAT_KNOWLEDGE_DIR` | no | A folder of your own answer files (`*.md`) for the study bot; a file with the same name as a built-in one replaces it |
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

73 tests cover open access and the browser security headers, form validation, upload checks (PDF and image headers,
size limits, SVG refusal), YouTube link parsing, GitHub link safety, the study bot's word matching, spelling tolerance and answer-file reading. They never touch your real database.

## Code execution in short

- **JavaScript and Python** run inside your own browser in an isolated worker (10 second limit, no network).
- **Java, C and C++** run in a separate service (`../code-runner`) inside locked-down Docker containers.
- **R and SQL** can be written and saved but not run (no sandbox exists for them yet).
- User code is never executed inside the Spring Boot application itself.

## Replace the placeholders

- Logos: `src/main/resources/static/img/ds-logo.svg` is the Data Science logo in the site header. The ID card uses `img/mbu-logo.png` (the university logo, small because it was cut from a screenshot; replace it with a sharper PNG of the same name). Replace the file (keeping the name) to change it.
- Your details: open **Profile**, then **Edit**. Nothing is pre-filled.
- Your syllabus: log in and use **Modules**, then **Add Module**.

## Study Bot (offline)

A round **Ask Bot** button in the bottom-right corner opens a chat window. A student types a question in their own words
and the bot replies from a ready-made list of answers. It is **free, works offline and needs no API key**: it uses no AI,
it only compares the words of the question with the stored answers (it forgives small spelling mistakes and short forms like "df" or "ml").

- **What it knows:** the answer files in `src/main/resources/chat/knowledge/` (Python, data analysis, SQL, statistics and machine learning, tools, this portal, the college, small talk) **and everything on the website itself**: the modules and their PDFs, every lab experiment (aim, syntax, code, output, result), the tools, the resources (free and paid courses, W3Schools, YouTube) and the notes. The website part is read from the database automatically (refreshed about once a minute), so anything you add is known to the bot without editing any file. Try *"experiment 4b"*, *"what is in module 1"* or *"free courses"*.
- **If it is not sure:** it asks "did you mean...?" with clickable questions. If nothing matches it says so honestly. It never invents an answer.
- **Add your own answers:** write an entry in any `.md` file there:

  ```
  ## When is the Data Science lab?
  keywords: lab timing, lab schedule, practical
  questions: what time is the DS lab | lab timetable

  The DS lab is on Tuesdays, 2 pm to 5 pm in Lab 3.
  ```

  The `keywords` line matters most: list the words students will use. Restart the app to load changes.
- **College details:** the bot only knows the college facts you write. Fill `70-college.md` with official details (departments, exam rules, contacts, timings). To keep your own files outside the project, set `CHAT_KNOWLEDGE_DIR` to a folder; a file there with the same name as a built-in one replaces it.
