package com.mbu.dslibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ExperimentRequest;
import com.mbu.dslibrary.model.Experiment;
import com.mbu.dslibrary.service.ExperimentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService service;

    public ExperimentController(ExperimentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Experiment> search(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) String moduleId,
                                   @RequestParam(required = false) String language) {
        return service.search(q, moduleId, language);
    }

    @GetMapping("/{id}")
    public Experiment get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Experiment create(@Valid @RequestBody ExperimentRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public Experiment update(@PathVariable String id, @Valid @RequestBody ExperimentRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/output-image")
    public Experiment uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return service.setOutputImage(id, file);
    }

    @DeleteMapping("/{id}/output-image")
    public Experiment deleteImage(@PathVariable String id) { return service.removeOutputImage(id); }
}
