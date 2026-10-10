package com.mbu.dslibrary.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mbu.dslibrary.dto.NoteRequest;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.Note;
import com.mbu.dslibrary.repository.ModuleRepository;
import com.mbu.dslibrary.repository.NoteRepository;

@Service
public class NoteService {

    private final NoteRepository repo;
    private final ModuleRepository modules;
    private final MongoTemplate mongo;

    public NoteService(NoteRepository repo, ModuleRepository modules, MongoTemplate mongo) {
        this.repo = repo;
        this.modules = modules;
        this.mongo = mongo;
    }

    /** All filters are optional; text search looks in title, content and tags. Newest edits first. */
    public List<Note> search(String q, String moduleId, String subject, String tag) {
        List<Criteria> parts = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            Pattern p = Pattern.compile(Pattern.quote(q.trim()), Pattern.CASE_INSENSITIVE);
            parts.add(new Criteria().orOperator(
                    Criteria.where("title").regex(p),
                    Criteria.where("content").regex(p),
                    Criteria.where("tags").regex(p)));
        }
        if (moduleId != null && !moduleId.isBlank()) parts.add(Criteria.where("moduleId").is(moduleId));
        if (subject != null && !subject.isBlank()) parts.add(Criteria.where("subject").is(subject));
        if (tag != null && !tag.isBlank()) parts.add(Criteria.where("tags").is(tag.trim().toLowerCase()));
        Query query = new Query();
        if (!parts.isEmpty()) query.addCriteria(new Criteria().andOperator(parts.toArray(new Criteria[0])));
        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        return mongo.find(query, Note.class);
    }

    public Note get(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Note not found"));
    }

    public Note create(NoteRequest req) {
        Note n = new Note();
        apply(n, req);
        return repo.save(n);
    }

    public Note update(String id, NoteRequest req) {
        Note n = get(id);
        apply(n, req);
        return repo.save(n);
    }

    public void delete(String id) {
        repo.delete(get(id));
    }

    public List<String> subjects() {
        return mongo.findDistinct(new Query(Criteria.where("subject").nin(null, "")), "subject", Note.class, String.class)
                .stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public List<String> tags() {
        return mongo.findDistinct(new Query(), "tags", Note.class, String.class)
                .stream().sorted().toList();
    }

    private void apply(Note n, NoteRequest req) {
        String moduleId = req.getModuleId() == null || req.getModuleId().isBlank() ? null : req.getModuleId();
        if (moduleId != null && !modules.existsById(moduleId)) {
            throw new IllegalArgumentException("The selected module does not exist.");
        }
        Set<String> tags = new LinkedHashSet<>();
        for (String t : req.getTags()) {
            if (t != null && !t.isBlank()) tags.add(t.trim().toLowerCase());
        }
        n.setTitle(req.getTitle().trim());
        n.setSubject(req.getSubject() == null ? "" : req.getSubject().trim());
        n.setModuleId(moduleId);
        n.setContent(req.getContent());
        n.setTags(new ArrayList<>(tags));
    }
}
