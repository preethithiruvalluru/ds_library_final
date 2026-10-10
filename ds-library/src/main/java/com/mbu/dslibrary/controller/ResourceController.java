package com.mbu.dslibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ResourceRequest;
import com.mbu.dslibrary.model.Resource;
import com.mbu.dslibrary.service.ResourceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resource> search(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) String subject,
                                 @RequestParam(required = false) String moduleId,
                                 @RequestParam(required = false) String tag) {
        return service.search(q, type, subject, moduleId, tag);
    }

    @GetMapping("/subjects")
    public List<String> subjects() { return service.subjects(); }

    @GetMapping("/tags")
    public List<String> tags() { return service.tags(); }

    @GetMapping("/{id}")
    public Resource get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resource create(@Valid @RequestBody ResourceRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public Resource update(@PathVariable String id, @Valid @RequestBody ResourceRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/file")
    public Resource uploadFile(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return service.setFile(id, file);
    }

    @DeleteMapping("/{id}/file")
    public Resource deleteFile(@PathVariable String id) { return service.removeFile(id); }
}
