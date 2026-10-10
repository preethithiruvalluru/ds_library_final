package com.mbu.dslibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbu.dslibrary.dto.ToolRequest;
import com.mbu.dslibrary.model.Tool;
import com.mbu.dslibrary.service.ToolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService service;

    public ToolController(ToolService service) {
        this.service = service;
    }

    @GetMapping
    public List<Tool> search(@RequestParam(required = false) String q, @RequestParam(required = false) String category) {
        return service.search(q, category);
    }

    @GetMapping("/{id}")
    public Tool get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tool create(@Valid @RequestBody ToolRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public Tool update(@PathVariable String id, @Valid @RequestBody ToolRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
