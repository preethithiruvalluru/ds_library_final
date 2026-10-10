package com.mbu.dslibrary.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mbu.dslibrary.dto.ToolRequest;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.Tool;
import com.mbu.dslibrary.repository.ToolRepository;

@Service
public class ToolService {

    private final ToolRepository repo;
    private final MongoTemplate mongo;

    public ToolService(ToolRepository repo, MongoTemplate mongo) {
        this.repo = repo;
        this.mongo = mongo;
    }

    public List<Tool> search(String q, String category) {
        List<Criteria> parts = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            Pattern p = Pattern.compile(Pattern.quote(q.trim()), Pattern.CASE_INSENSITIVE);
            parts.add(new Criteria().orOperator(Criteria.where("name").regex(p), Criteria.where("description").regex(p)));
        }
        if (category != null && !category.isBlank()) parts.add(Criteria.where("category").is(category));
        Query query = new Query().with(Sort.by("name"));
        if (!parts.isEmpty()) query.addCriteria(new Criteria().andOperator(parts.toArray(new Criteria[0])));
        return mongo.find(query, Tool.class);
    }

    public Tool get(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Tool not found"));
    }

    public Tool create(ToolRequest req) {
        Tool t = new Tool();
        apply(t, req);
        return repo.save(t);
    }

    public Tool update(String id, ToolRequest req) {
        Tool t = get(id);
        apply(t, req);
        return repo.save(t);
    }

    public void delete(String id) {
        repo.delete(get(id));
    }

    private void apply(Tool t, ToolRequest req) {
        t.setName(req.getName().trim());
        t.setCategory(req.getCategory().trim());
        t.setDescription(req.getDescription() == null ? "" : req.getDescription().trim());
        t.setOfficialUrl(req.getOfficialUrl().trim());
        t.setIcon(req.getIcon() == null ? "" : req.getIcon().trim());
    }
}
