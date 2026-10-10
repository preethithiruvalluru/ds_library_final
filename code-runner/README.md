# Code runner

A small separate service that runs Java, C and C++ programs for the Code Editor.
The main app (`ds-library`) never runs user code itself; it forwards requests here.

Every run happens in a brand-new Docker container with:

| Protection | Setting |
|---|---|
| Network | none (`--network none`) |
| Filesystem | read-only root; code mounted read-only; a 128 MB in-memory `/tmp` |
| Memory | 512 MB (Java) / 256 MB (C, C++), swap disabled |
| CPU / processes | 1 CPU, at most 128 processes |
| Privileges | all Linux capabilities dropped, `no-new-privileges`, runs as user 65534 (not root) |
| Time | 20 seconds, then the container is killed |
| Output | first 100,000 characters of stdout and of stderr |
| Concurrency | 2 programs at a time, others get HTTP 429 |

The service listens on `127.0.0.1:8090` only and every call needs the `X-Runner-Token` header.

## Start it

Docker Desktop must be running ("Engine running").

```powershell
$env:RUNNER_TOKEN = "choose-a-long-random-secret-at-least-24-chars"
cd code-runner
mvn spring-boot:run
```

If Docker is not on PATH for this process, also set `$env:DOCKER_CMD` to the full path of `docker.exe`.
The first start downloads the images `eclipse-temurin:21-jdk` and `gcc:13` (a few minutes). A language shows as ready in the editor when its image is available.

## Tell the main app about it

In the terminal where you start `ds-library`, set the SAME token:

```powershell
$env:RUNNER_URL = "http://localhost:8090"
$env:RUNNER_TOKEN = "choose-a-long-random-secret-at-least-24-chars"
cd ds-library
mvn spring-boot:run
```

Anyone who can open the portal can run code through this service, so only connect it to a portal that is not public.

## Adding a language

Add an entry to `LANGS` in `DockerRunner.java` (image, file name, command, memory) and to the allowed list in `ExecutionController` of the main app, then test it the same way before relying on it.
