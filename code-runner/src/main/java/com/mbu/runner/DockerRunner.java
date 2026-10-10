package com.mbu.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Runs one program per request inside a fresh Docker container with these limits:
 * no network, read-only root filesystem, a small writable /tmp in memory, memory and CPU caps,
 * a process-count cap, no Linux capabilities, unprivileged user, a hard time limit and an output cap.
 * The user's code is mounted READ-ONLY from a temporary folder that is deleted after the run.
 */
@Service
public class DockerRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerRunner.class);
    static final Duration TIME_LIMIT = Duration.ofSeconds(20);
    static final int MAX_STREAM_CHARS = 100_000;

    public record Lang(String id, String label, String image, String fileName, List<String> command, int memoryMb) {}

    static final Map<String, Lang> LANGS = Map.of(
        "java", new Lang("java", "Java", "eclipse-temurin:21-jdk", "Main.java",
            List.of("sh", "-c", "javac -d /tmp /src/Main.java && java -Xmx256m -cp /tmp Main"), 512),
        "c", new Lang("c", "C", "gcc:13", "main.c",
            List.of("sh", "-c", "gcc -O2 -Wall -o /tmp/prog /src/main.c -lm && /tmp/prog"), 256),
        "cpp", new Lang("cpp", "C++", "gcc:13", "main.cpp",
            List.of("sh", "-c", "g++ -O2 -Wall -o /tmp/prog /src/main.cpp && /tmp/prog"), 256));

    private final String docker;
    private final Map<String, Boolean> ready = new ConcurrentHashMap<>();
    private final Semaphore slots = new Semaphore(2);   // at most 2 programs at the same time

    public DockerRunner(@Value("${runner.docker-command:docker}") String docker) {
        this.docker = docker;
    }

    /** Pulls the language images in the background so the first real run is not delayed by a download. */
    @EventListener(ApplicationReadyEvent.class)
    public void prepareImages() {
        Thread t = new Thread(() -> {
            for (Lang lang : LANGS.values()) {
                try {
                    ready.put(lang.id(), prepare(lang.image()));
                } catch (Exception e) {
                    log.warn("Could not prepare image {}: {}", lang.image(), e.getMessage());
                    ready.put(lang.id(), false);
                }
            }
        }, "image-prepare");
        t.setDaemon(true);
        t.start();
    }

    private boolean prepare(String image) throws IOException, InterruptedException {
        if (exec(List.of(docker, "image", "inspect", image), Duration.ofSeconds(20)).exitCode() == 0) return true;
        log.info("Downloading image {} (first time only)...", image);
        boolean ok = exec(List.of(docker, "pull", image), Duration.ofMinutes(15)).exitCode() == 0;
        log.info("Image {} ready: {}", image, ok);
        return ok;
    }

    public List<Map<String, Object>> languages() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Lang lang : LANGS.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", lang.id());
            m.put("label", lang.label());
            m.put("ready", Boolean.TRUE.equals(ready.get(lang.id())));
            list.add(m);
        }
        return list;
    }

    public RunResult run(String languageId, String code, String stdin) throws IOException, InterruptedException {
        Lang lang = LANGS.get(languageId);
        if (lang == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported language: " + languageId);
        }
        if (!Boolean.TRUE.equals(ready.get(lang.id()))) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    lang.label() + " is still being prepared, or Docker is not running. Try again in a minute.");
        }
        if (!slots.tryAcquire()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "The runner is busy. Try again in a few seconds.");
        }
        Path dir = Files.createTempDirectory("run-");
        try {
            Files.writeString(dir.resolve(lang.fileName()), code, StandardCharsets.UTF_8);
            String name = "run-" + UUID.randomUUID();
            String mem = lang.memoryMb() + "m";

            List<String> cmd = new ArrayList<>(List.of(docker, "run", "--rm", "-i", "--name", name,
                    "--network", "none",
                    "--read-only",
                    "--tmpfs", "/tmp:rw,exec,nosuid,size=128m",
                    "--memory", mem, "--memory-swap", mem,
                    "--cpus", "1",
                    "--pids-limit", "128",
                    "--cap-drop", "ALL",
                    "--security-opt", "no-new-privileges",
                    "--user", "65534:65534",
                    "-e", "HOME=/tmp",
                    "-v", dir.toAbsolutePath() + ":/src:ro",
                    "--workdir", "/tmp",
                    lang.image()));
            cmd.addAll(lang.command());

            Process p = new ProcessBuilder(cmd).start();
            try (OutputStream in = p.getOutputStream()) {
                in.write(stdin.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                // The program may have exited before reading its input. That is fine.
            }
            CompletableFuture<Capture> out = CompletableFuture.supplyAsync(() -> capture(p.getInputStream()));
            CompletableFuture<Capture> err = CompletableFuture.supplyAsync(() -> capture(p.getErrorStream()));

            boolean finished = p.waitFor(TIME_LIMIT.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                exec(List.of(docker, "kill", name), Duration.ofSeconds(10));   // stop the container itself
                p.destroyForcibly();
                p.waitFor(5, TimeUnit.SECONDS);
            }
            Capture o = out.get(5, TimeUnit.SECONDS);
            Capture e = err.get(5, TimeUnit.SECONDS);
            return new RunResult(finished ? p.exitValue() : null, !finished, o.text(), e.text(), o.truncated() || e.truncated());
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new IOException("Could not read the program output: " + e.getMessage(), e);
        } finally {
            slots.release();
            deleteQuietly(dir);
        }
    }

    public record RunResult(Integer exitCode, boolean timedOut, String stdout, String stderr, boolean truncated) {}

    private record Capture(String text, boolean truncated) {}

    /** Reads a stream fully but keeps only the first MAX_STREAM_CHARS bytes, so a noisy program cannot fill memory. */
    private static Capture capture(InputStream in) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        boolean truncated = false;
        byte[] chunk = new byte[8192];
        try {
            int n;
            while ((n = in.read(chunk)) != -1) {
                int room = MAX_STREAM_CHARS - buf.size();
                if (room > 0) {
                    buf.write(chunk, 0, Math.min(n, room));
                }
                if (n > room) truncated = true;
            }
        } catch (IOException ignored) {
            // stream closed
        }
        return new Capture(buf.toString(StandardCharsets.UTF_8), truncated);
    }

    private record ProcessOutput(int exitCode, String text) {}

    private ProcessOutput exec(List<String> cmd, Duration timeout) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        CompletableFuture<Capture> out = CompletableFuture.supplyAsync(() -> capture(p.getInputStream()));
        if (!p.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            return new ProcessOutput(-1, "timed out");
        }
        try {
            return new ProcessOutput(p.exitValue(), out.get(5, TimeUnit.SECONDS).text());
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            return new ProcessOutput(p.exitValue(), "");
        }
    }

    private static void deleteQuietly(Path dir) {
        try {
            Files.walk(dir).sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException ignored) {
            // best effort: the folder is in the temp directory
        }
    }
}
