package com.mbu.dslibrary.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Talks to the separate code-runner service (see the code-runner folder). This app never runs code itself.
 * If RUNNER_URL or RUNNER_TOKEN is not set, the runner is treated as "not configured".
 */
@Service
public class CodeRunnerClient {

    /** Result of a failed call: an HTTP status and a message that is safe to show the user. */
    public static class RunnerFailure extends RuntimeException {
        private final int status;

        public RunnerFailure(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() { return status; }
    }

    private final String url;
    private final String token;
    private final RestClient client;
    private final ObjectMapper json;

    public CodeRunnerClient(@Value("${runner.url:}") String url,
                            @Value("${runner.token:}") String token,
                            ObjectMapper json) {
        this.url = url == null ? "" : url.replaceAll("/+$", "");
        this.token = token == null ? "" : token;
        this.json = json;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(40));   // the runner's own limit is 20 seconds
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    public boolean configured() {
        return !url.isBlank() && !token.isBlank();
    }

    /** Languages the runner offers, each with a "ready" flag. Empty when the runner is not configured or unreachable. */
    public List<Map<String, Object>> languages() {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!configured()) return result;
        try {
            JsonNode body = client.get().uri(url + "/languages")
                    .header("X-Runner-Token", token)
                    .retrieve().body(JsonNode.class);
            if (body == null) return result;
            for (JsonNode n : body.path("languages")) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", n.path("id").asText());
                m.put("label", n.path("label").asText());
                m.put("ready", n.path("ready").asBoolean(false));
                result.add(m);
            }
        } catch (RestClientException e) {
            // unreachable: report no languages rather than failing the whole page
        }
        return result;
    }

    public JsonNode run(String language, String code, String stdin) {
        if (!configured()) {
            throw new RunnerFailure(501, "No secure code runner is configured on this server.");
        }
        Map<String, String> body = Map.of("language", language, "code", code, "stdin", stdin == null ? "" : stdin);
        try {
            return client.post().uri(url + "/run")
                    .header("X-Runner-Token", token)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(body)
                    .retrieve().body(JsonNode.class);
        } catch (RestClientResponseException e) {
            String message = "The code runner reported an error.";
            try {
                String text = json.readTree(e.getResponseBodyAsString()).path("message").asText("");
                if (!text.isBlank()) message = text;
            } catch (Exception ignored) {
                // body was not JSON
            }
            throw new RunnerFailure(e.getStatusCode().value(), message);
        } catch (RestClientException e) {
            throw new RunnerFailure(502, "The code runner is not reachable. Start it first (see the code-runner folder).");
        }
    }
}
