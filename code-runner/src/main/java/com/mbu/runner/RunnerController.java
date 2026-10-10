package com.mbu.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Only the main app calls this, with the shared token. Nothing here runs code itself; it all goes through DockerRunner. */
@RestController
public class RunnerController {

    private static final int MAX_CODE_CHARS = 100_000;
    private static final int MAX_STDIN_CHARS = 10_000;

    public record RunRequest(
            @NotBlank String language,
            @Size(max = MAX_CODE_CHARS, message = "Code is too long") String code,
            @Size(max = MAX_STDIN_CHARS, message = "Input is too long") String stdin) {}

    private final DockerRunner runner;
    private final String token;

    public RunnerController(DockerRunner runner, @Value("${runner.token:}") String token) {
        this.runner = runner;
        this.token = token;
    }

    @PostConstruct
    void requireToken() {
        if (token == null || token.length() < 24) {
            throw new IllegalStateException("Set RUNNER_TOKEN to a secret of at least 24 characters before starting the runner.");
        }
    }

    @GetMapping("/languages")
    public Map<String, Object> languages(@RequestHeader(value = "X-Runner-Token", required = false) String sent) {
        checkToken(sent);
        return Map.of("languages", runner.languages());
    }

    @PostMapping("/run")
    public DockerRunner.RunResult run(@RequestHeader(value = "X-Runner-Token", required = false) String sent,
                                      @Valid @RequestBody RunRequest req) throws IOException, InterruptedException {
        checkToken(sent);
        String code = req.code() == null ? "" : req.code();
        String stdin = req.stdin() == null ? "" : req.stdin();
        if (code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no code to run.");
        }
        return runner.run(req.language(), code, stdin);
    }

    /** Constant-time comparison so the token cannot be guessed by timing. */
    private void checkToken(String sent) {
        byte[] a = token.getBytes(StandardCharsets.UTF_8);
        byte[] b = (sent == null ? "" : sent).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(a, b)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or wrong runner token.");
        }
    }
}
