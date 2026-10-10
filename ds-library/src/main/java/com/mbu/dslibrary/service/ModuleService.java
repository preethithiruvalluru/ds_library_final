package com.mbu.dslibrary.service;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ModuleRequest;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.Module;
import com.mbu.dslibrary.model.Note;
import com.mbu.dslibrary.model.StoredFile;
import com.mbu.dslibrary.repository.ModuleRepository;

@Service
public class ModuleService {

    private final ModuleRepository repo;
    private final FileStorageService files;
    private final MongoTemplate mongo;

    public ModuleService(ModuleRepository repo, FileStorageService files, MongoTemplate mongo) {
        this.repo = repo;
        this.files = files;
        this.mongo = mongo;
    }

    public List<Module> list() {
        return repo.findAllByOrderBySubjectAscModuleNumberAsc();
    }

    public Module get(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Module not found"));
    }

    public Module create(ModuleRequest req) {
        Module m = new Module();
        apply(m, req);
        return repo.save(m);
    }

    public Module update(String id, ModuleRequest req) {
        Module m = get(id);
        apply(m, req);
        return repo.save(m);
    }

    /** Deletes the module and every PDF that belongs to it. */
    public void delete(String id) {
        Module m = get(id);
        if (m.getSyllabus() != null) files.delete(m.getSyllabus().getFileId());
        m.getMaterials().forEach(f -> files.delete(f.getFileId()));
        repo.delete(m);
        // Notes keep existing, they just lose their module link.
        mongo.updateMulti(Query.query(Criteria.where("moduleId").is(id)), new Update().unset("moduleId"), Note.class);
    }

    public Module setSyllabus(String id, MultipartFile file) {
        Module m = get(id);
        StoredFile saved = files.storePdf(file, "Syllabus");
        if (m.getSyllabus() != null) files.delete(m.getSyllabus().getFileId());
        m.setSyllabus(saved);
        return repo.save(m);
    }

    public Module removeSyllabus(String id) {
        Module m = get(id);
        if (m.getSyllabus() != null) files.delete(m.getSyllabus().getFileId());
        m.setSyllabus(null);
        return repo.save(m);
    }

    public Module addMaterial(String id, MultipartFile file, String title) {
        Module m = get(id);
        m.getMaterials().add(files.storePdf(file, title));
        return repo.save(m);
    }

    public Module removeMaterial(String id, String fileId) {
        Module m = get(id);
        boolean removed = m.getMaterials().removeIf(f -> f.getFileId().equals(fileId));
        if (!removed) throw new NotFoundException("Material not found");
        files.delete(fileId);
        return repo.save(m);
    }

    private void apply(Module m, ModuleRequest req) {
        m.setModuleNumber(req.getModuleNumber());
        m.setName(req.getName().trim());
        m.setSubject(req.getSubject().trim());
        m.setDescription(req.getDescription() == null ? "" : req.getDescription().trim());
    }
}
