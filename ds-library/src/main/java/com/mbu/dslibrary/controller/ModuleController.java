package com.mbu.dslibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ModuleRequest;
import com.mbu.dslibrary.model.Module;
import com.mbu.dslibrary.service.ModuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService service;

    public ModuleController(ModuleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Module> list() { return service.list(); }

    @GetMapping("/{id}")
    public Module get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Module create(@Valid @RequestBody ModuleRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public Module update(@PathVariable String id, @Valid @RequestBody ModuleRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/syllabus")
    public Module uploadSyllabus(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return service.setSyllabus(id, file);
    }

    @DeleteMapping("/{id}/syllabus")
    public Module deleteSyllabus(@PathVariable String id) { return service.removeSyllabus(id); }

    @PostMapping("/{id}/materials")
    public Module uploadMaterial(@PathVariable String id, @RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "title", required = false) String title) {
        return service.addMaterial(id, file, title);
    }

    @DeleteMapping("/{id}/materials/{fileId}")
    public Module deleteMaterial(@PathVariable String id, @PathVariable String fileId) {
        return service.removeMaterial(id, fileId);
    }
}
