package com.mbu.dslibrary.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbu.dslibrary.service.FileStorageService;

/** Streams stored files. Shown inside the page by default, downloaded with ?download=true. */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService files;

    public FileController(FileStorageService files) {
        this.files = files;
    }

    /** Which of these file ids are not stored any more? One quick call instead of opening every file. */
    @GetMapping("/missing")
    public List<String> missing(@RequestParam String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).limit(200)
                .filter(id -> !files.exists(id)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> get(@PathVariable String id,
                                                   @RequestParam(defaultValue = "false") boolean download) throws Exception {
        GridFsResource res = files.load(id);
        ContentDisposition cd = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(res.getFilename()).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(res.getContentType()))
                .contentLength(res.contentLength())
                .body(new InputStreamResource(res.getInputStream()));
    }
}
