package com.mbu.dslibrary.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simple check that the app is up and MongoDB is reachable. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final MongoTemplate mongo;

    public HealthController(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("app", "UP");
        try {
            mongo.getDb().runCommand(new Document("ping", 1));
            body.put("database", "UP");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("database", "DOWN");
            body.put("message", "Cannot reach MongoDB. Is it running and is MONGODB_URI correct?");
            return ResponseEntity.status(503).body(body);
        }
    }
}
