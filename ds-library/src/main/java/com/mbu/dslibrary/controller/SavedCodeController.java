package com.mbu.dslibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbu.dslibrary.dto.SavedCodeRequest;
import com.mbu.dslibrary.model.SavedCode;
import com.mbu.dslibrary.service.SavedCodeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/code")
public class SavedCodeController {

    private final SavedCodeService service;

    public SavedCodeController(SavedCodeService service) {
        this.service = service;
    }

    @GetMapping
    public List<SavedCode> list() { return service.list(); }

    @GetMapping("/{id}")
    public SavedCode get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedCode create(@Valid @RequestBody SavedCodeRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public SavedCode update(@PathVariable String id, @Valid @RequestBody SavedCodeRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
