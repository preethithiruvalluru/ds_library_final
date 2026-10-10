package com.mbu.dslibrary.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbu.dslibrary.service.CodeRunnerClient;
import com.mbu.dslibrary.service.CodeRunnerClient.RunnerFailure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Runs code through the separate code-runner service. This app never executes user code itself.
 * Only the languages the runner reports as ready can run. Running requires the admin login.
 */
@RestController
@RequestMapping("/api/execute")
public class ExecutionController {

    private static final String NOT_CONFIGURED =
            "No secure code runner is configured on this server, so this language cannot be run here. "
            + "Your code can still be written and saved.";

    public record ExecuteRequest(
            @NotBlank(message = "Language is required")
            @Pattern(regexp = "java|c|cpp", message = "This language cannot run on the server")
            String language,
            @Size(max = 100000, message = "Code is too long (max 100000 characters)") String code,
            @Size(max = 10000, message = "Input is too long (max 10000 characters)") String stdin) {}

    private final CodeRunnerClient runner;
    private final ObjectMapper json;

    public ExecutionController(CodeRunnerClient runner, ObjectMapper json) {
        this.runner = runner;
        this.json = json;
    }

    @GetMapping("/languages")
    public Map<String, Object> languages() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("runnerConfigured", runner.configured());
        body.put("serverLanguages", runner.languages());
        body.put("message", runner.configured() ? "" : NOT_CONFIGURED);
        return body;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> run(@Valid @RequestBody ExecuteRequest req) {
        if (req.code() == null || req.code().isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "There is no code to run.");
        }
        try {
            JsonNode r = runner.run(req.language(), req.code(), req.stdin());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("stdout", r.path("stdout").asText(""));
            body.put("stderr", r.path("stderr").asText(""));
            body.put("exitCode", r.path("exitCode").isNull() ? null : r.path("exitCode").asInt());
            body.put("timedOut", r.path("timedOut").asBoolean(false));
            body.put("truncated", r.path("truncated").asBoolean(false));
            return ResponseEntity.ok(body);
        } catch (RunnerFailure e) {
            return error(HttpStatus.valueOf(e.getStatus() >= 400 && e.getStatus() < 600 ? e.getStatus() : 502), e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
