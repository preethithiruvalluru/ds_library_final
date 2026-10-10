package com.mbu.dslibrary.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/** A course module (unit) with an optional syllabus PDF and study-material PDFs. */
@Document(collection = "modules")
public class Module {
    @Id
    private String id;
    private int moduleNumber;
    private String name;
    private String subject;
    private String description;
    private StoredFile syllabus;
    private List<StoredFile> materials = new ArrayList<>();
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getModuleNumber() { return moduleNumber; }
    public void setModuleNumber(int moduleNumber) { this.moduleNumber = moduleNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public StoredFile getSyllabus() { return syllabus; }
    public void setSyllabus(StoredFile syllabus) { this.syllabus = syllabus; }
    public List<StoredFile> getMaterials() { return materials; }
    public void setMaterials(List<StoredFile> materials) { this.materials = materials; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
