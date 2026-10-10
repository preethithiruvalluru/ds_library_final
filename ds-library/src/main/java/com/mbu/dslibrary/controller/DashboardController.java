package com.mbu.dslibrary.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbu.dslibrary.model.Module;
import com.mbu.dslibrary.model.StoredFile;
import com.mbu.dslibrary.repository.ModuleRepository;
import com.mbu.dslibrary.repository.NoteRepository;
import com.mbu.dslibrary.repository.ResourceRepository;

/** Numbers and "recent" lists for the Home page, straight from the database. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ModuleRepository modules;
    private final NoteRepository notes;
    private final ResourceRepository resources;
    private final MongoTemplate mongo;

    public DashboardController(ModuleRepository modules, NoteRepository notes, ResourceRepository resources, MongoTemplate mongo) {
        this.resources = resources;
        this.modules = modules;
        this.notes = notes;
        this.mongo = mongo;
    }

    @GetMapping
    public Map<String, Object> dashboard() {
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("modules", modules.count());
        counts.put("notes", notes.count());
        counts.put("experiments", mongo.getCollection("experiments").countDocuments());
        counts.put("resources", resources.count());

        List<Map<String, Object>> recentPdfs = new ArrayList<>();
        for (Module m : modules.findAll()) {
            if (m.getSyllabus() != null) recentPdfs.add(pdf(m, m.getSyllabus(), "Syllabus"));
            for (StoredFile f : m.getMaterials()) recentPdfs.add(pdf(m, f, "Study material"));
        }
        resources.findAll().forEach(r -> {
            if (r.getFile() != null) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("fileId", r.getFile().getFileId());
                p.put("title", r.getTitle());
                p.put("moduleName", "Resources");
                p.put("kind", "Resource");
                p.put("uploadedAt", r.getFile().getUploadedAt() == null ? "" : r.getFile().getUploadedAt().toString());
                recentPdfs.add(p);
            }
        });
        recentPdfs.sort(Comparator.comparing((Map<String, Object> p) -> String.valueOf(p.get("uploadedAt"))).reversed());

        List<Document> experiments = mongo.find(
                new Query().with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(5), Document.class, "experiments");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("counts", counts);
        body.put("recentNotes", notes.findTop5ByOrderByCreatedAtDesc());
        body.put("recentExperiments", experiments.stream().map(d -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", d.getObjectId("_id").toHexString());
            e.put("experimentNumber", d.get("experimentNumber"));
            e.put("title", d.get("title"));
            return e;
        }).toList());
        body.put("recentPdfs", recentPdfs.stream().limit(5).toList());
        return body;
    }

    private Map<String, Object> pdf(Module m, StoredFile f, String kind) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("fileId", f.getFileId());
        p.put("title", "Syllabus".equals(kind) ? "Syllabus: " + m.getName() : f.getTitle());
        p.put("moduleName", m.getName());
        p.put("kind", kind);
        p.put("uploadedAt", f.getUploadedAt() == null ? "" : f.getUploadedAt().toString());
        return p;
    }
}
