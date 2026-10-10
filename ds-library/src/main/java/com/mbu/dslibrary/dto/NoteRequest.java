package com.mbu.dslibrary.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** What the browser sends when creating or editing a note. */
public class NoteRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long (max 200)")
    private String title;

    @Size(max = 150, message = "Subject is too long (max 150)")
    private String subject;

    private String moduleId;

    @NotBlank(message = "Content is required")
    @Size(max = 50000, message = "Content is too long (max 50000 characters)")
    private String content;

    @Size(max = 20, message = "Use at most 20 tags")
    private List<@Size(max = 40, message = "A tag is too long (max 40)") String> tags = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
