package com.mbu.dslibrary.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ExperimentRequest;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.Experiment;
import com.mbu.dslibrary.repository.ExperimentRepository;
import com.mbu.dslibrary.repository.ModuleRepository;

@Service
public class ExperimentService {

    private static final Pattern LEADING_NUMBER = Pattern.compile("^\\s*(\\d+)");

    private final ExperimentRepository repo;
    private final ModuleRepository modules;
    private final FileStorageService files;
    private final MongoTemplate mongo;

    public ExperimentService(ExperimentRepository repo, ModuleRepository modules, FileStorageService files, MongoTemplate mongo) {
        this.repo = repo;
        this.modules = modules;
        this.files = files;
        this.mongo = mongo;
    }

    /** Optional filters; text search looks in number, title, aim and source code. Sorted by experiment number. */
    public List<Experiment> search(String q, String moduleId, String language) {
        List<Criteria> parts = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            Pattern p = Pattern.compile(Pattern.quote(q.trim()), Pattern.CASE_INSENSITIVE);
            parts.add(new Criteria().orOperator(
                    Criteria.where("experimentNumber").regex(p),
                    Criteria.where("title").regex(p),
                    Criteria.where("aim").regex(p),
                    Criteria.where("sourceCode").regex(p)));
        }
        if (moduleId != null && !moduleId.isBlank()) parts.add(Criteria.where("moduleId").is(moduleId));
        if (language != null && !language.isBlank()) parts.add(Criteria.where("language").is(language));
        Query query = new Query();
        if (!parts.isEmpty()) query.addCriteria(new Criteria().andOperator(parts.toArray(new Criteria[0])));
        List<Experiment> list = mongo.find(query, Experiment.class);
        list.sort(Comparator.comparingInt((Experiment e) -> leadingNumber(e.getExperimentNumber()))
                .thenComparing(e -> String.valueOf(e.getExperimentNumber()), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public Experiment get(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Experiment not found"));
    }

    public Experiment create(ExperimentRequest req) {
        Experiment e = new Experiment();
        apply(e, req);
        return repo.save(e);
    }

    public Experiment update(String id, ExperimentRequest req) {
        Experiment e = get(id);
        apply(e, req);
        return repo.save(e);
    }

    public void delete(String id) {
        Experiment e = get(id);
        if (e.getOutputImage() != null) files.delete(e.getOutputImage().getFileId());
        repo.delete(e);
    }

    public Experiment setOutputImage(String id, MultipartFile file) {
        Experiment e = get(id);
        var saved = files.storeImage(file);
        if (e.getOutputImage() != null) files.delete(e.getOutputImage().getFileId());
        e.setOutputImage(saved);
        return repo.save(e);
    }

    public Experiment removeOutputImage(String id) {
        Experiment e = get(id);
        if (e.getOutputImage() != null) files.delete(e.getOutputImage().getFileId());
        e.setOutputImage(null);
        return repo.save(e);
    }

    private int leadingNumber(String s) {
        if (s == null) return Integer.MAX_VALUE;
        Matcher m = LEADING_NUMBER.matcher(s);
        if (!m.find()) return Integer.MAX_VALUE;
        try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ex) { return Integer.MAX_VALUE; }
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private void apply(Experiment e, ExperimentRequest req) {
        String moduleId = req.getModuleId() == null || req.getModuleId().isBlank() ? null : req.getModuleId();
        if (moduleId != null && !modules.existsById(moduleId)) {
            throw new IllegalArgumentException("The selected module does not exist.");
        }
        e.setExperimentNumber(req.getExperimentNumber().trim());
        e.setTitle(req.getTitle().trim());
        e.setAim(clean(req.getAim()));
        e.setRequirements(clean(req.getRequirements()));
        e.setProcedure(clean(req.getProcedure()));
        e.setSourceCode(req.getSourceCode() == null ? "" : req.getSourceCode());   // keep code exactly as typed
        e.setLanguage(req.getLanguage() == null ? "plaintext" : req.getLanguage());
        e.setOutputText(req.getOutputText() == null ? "" : req.getOutputText());
        e.setResult(clean(req.getResult()));
        e.setModuleId(moduleId);
        e.setGithubRepoUrl(clean(req.getGithubRepoUrl()));
    }
}
