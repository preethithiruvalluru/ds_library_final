package com.mbu.dslibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbu.dslibrary.dto.NoteRequest;
import com.mbu.dslibrary.model.Note;
import com.mbu.dslibrary.service.NoteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Note> search(@RequestParam(required = false) String q,
                             @RequestParam(required = false) String moduleId,
                             @RequestParam(required = false) String subject,
                             @RequestParam(required = false) String tag) {
        return service.search(q, moduleId, subject, tag);
    }

    @GetMapping("/subjects")
    public List<String> subjects() { return service.subjects(); }

    @GetMapping("/tags")
    public List<String> tags() { return service.tags(); }

    @GetMapping("/{id}")
    public Note get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(@Valid @RequestBody NoteRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public Note update(@PathVariable String id, @Valid @RequestBody NoteRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
