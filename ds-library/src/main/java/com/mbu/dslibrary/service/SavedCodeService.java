package com.mbu.dslibrary.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mbu.dslibrary.dto.SavedCodeRequest;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.model.SavedCode;
import com.mbu.dslibrary.repository.SavedCodeRepository;

@Service
public class SavedCodeService {

    private final SavedCodeRepository repo;

    public SavedCodeService(SavedCodeRepository repo) {
        this.repo = repo;
    }

    public List<SavedCode> list() {
        return repo.findAllByOrderByUpdatedAtDesc();
    }

    public SavedCode get(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Saved code not found"));
    }

    public SavedCode create(SavedCodeRequest req) {
        SavedCode s = new SavedCode();
        apply(s, req);
        return repo.save(s);
    }

    public SavedCode update(String id, SavedCodeRequest req) {
        SavedCode s = get(id);
        apply(s, req);
        return repo.save(s);
    }

    public void delete(String id) {
        repo.delete(get(id));
    }

    private void apply(SavedCode s, SavedCodeRequest req) {
        s.setTitle(req.getTitle().trim());
        s.setLanguage(req.getLanguage());
        s.setCode(req.getCode() == null ? "" : req.getCode());   // code is kept exactly as typed
        s.setStdin(req.getStdin() == null ? "" : req.getStdin());
    }
}
