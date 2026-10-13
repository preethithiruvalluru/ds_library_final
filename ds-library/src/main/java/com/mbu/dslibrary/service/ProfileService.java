package com.mbu.dslibrary.service;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ProfileRequest;
import com.mbu.dslibrary.model.StudentProfile;
import com.mbu.dslibrary.repository.ProfileRepository;

@Service
public class ProfileService {

    private final ProfileRepository repo;
    private final FileStorageService files;

    public ProfileService(ProfileRepository repo, FileStorageService files) {
        this.repo = repo;
        this.files = files;
    }

    /** Returns the saved profile, or an empty one (nothing invented) if you have not filled it in yet. */
    public StudentProfile get() {
        return repo.findById(StudentProfile.MAIN_ID).orElseGet(StudentProfile::new);
    }

    public StudentProfile update(ProfileRequest req) {
        StudentProfile p = get();
        p.setName(clean(req.getName()));
        p.setRollNumber(clean(req.getRollNumber()));
        p.setDepartment(clean(req.getDepartment()));
        p.setSection(clean(req.getSection()));
        p.setProgram(clean(req.getProgram()));
        p.setAcademicYear(clean(req.getAcademicYear()));
        p.setSemester(clean(req.getSemester()));
        p.setAdmissionYear(clean(req.getAdmissionYear()));
        p.setValidTill(clean(req.getValidTill()));
        p.setCollege(clean(req.getCollege()));
        p.setMentorName(clean(req.getMentorName()));
        p.setEmail(clean(req.getEmail()));
        p.setGithubUrl(clean(req.getGithubUrl()));
        p.setLinkedinUrl(clean(req.getLinkedinUrl()));
        Set<String> skills = new LinkedHashSet<>();
        for (String s : req.getSkills()) {
            if (s != null && !s.isBlank()) skills.add(s.trim());
        }
        p.setSkills(new java.util.ArrayList<>(skills));
        return save(p);
    }

    public StudentProfile setPhoto(MultipartFile file) {
        StudentProfile p = get();
        var saved = files.storeImage(file);
        if (p.getPhoto() != null) files.delete(p.getPhoto().getFileId());
        p.setPhoto(saved);
        return save(p);
    }

    public StudentProfile removePhoto() {
        StudentProfile p = get();
        if (p.getPhoto() != null) files.delete(p.getPhoto().getFileId());
        p.setPhoto(null);
        return save(p);
    }

    public StudentProfile setResume(MultipartFile file) {
        StudentProfile p = get();
        var saved = files.storePdf(file, "Resume");
        if (p.getResume() != null) files.delete(p.getResume().getFileId());
        p.setResume(saved);
        return save(p);
    }

    public StudentProfile removeResume() {
        StudentProfile p = get();
        if (p.getResume() != null) files.delete(p.getResume().getFileId());
        p.setResume(null);
        return save(p);
    }

    /** The id is fixed ("main"), so Spring does not treat the first save as new; set createdAt ourselves. */
    private StudentProfile save(StudentProfile p) {
        if (p.getCreatedAt() == null) p.setCreatedAt(java.time.Instant.now());
        return repo.save(p);
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
