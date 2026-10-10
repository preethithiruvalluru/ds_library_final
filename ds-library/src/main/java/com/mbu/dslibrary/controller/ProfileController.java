package com.mbu.dslibrary.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mbu.dslibrary.dto.ProfileRequest;
import com.mbu.dslibrary.model.StudentProfile;
import com.mbu.dslibrary.service.ProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public StudentProfile get() { return service.get(); }

    @PutMapping
    public StudentProfile update(@Valid @RequestBody ProfileRequest req) { return service.update(req); }

    @PostMapping("/photo")
    public StudentProfile uploadPhoto(@RequestParam("file") MultipartFile file) { return service.setPhoto(file); }

    @DeleteMapping("/photo")
    public StudentProfile deletePhoto() { return service.removePhoto(); }

    @PostMapping("/resume")
    public StudentProfile uploadResume(@RequestParam("file") MultipartFile file) { return service.setResume(file); }

    @DeleteMapping("/resume")
    public StudentProfile deleteResume() { return service.removeResume(); }
}
