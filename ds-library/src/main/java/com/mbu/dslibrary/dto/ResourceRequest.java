package com.mbu.dslibrary.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** What the browser sends when creating or editing a resource. */
public class ResourceRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long (max 200)")
    private String title;

    @Size(max = 1000, message = "Description is too long (max 1000)")
    private String description;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "PDF|LINK|DOCUMENTATION|TUTORIAL|YOUTUBE|MATERIAL", message = "Unsupported resource type")
    private String type;

    @Size(max = 150, message = "Subject is too long (max 150)")
    private String subject;

    private String moduleId;

    @Pattern(regexp = "^$|^https?://[^\\s]+$", message = "URL must start with http:// or https://")
    @Size(max = 500, message = "URL is too long (max 500)")
    private String url;

    @Size(max = 20, message = "Use at most 20 tags")
    private List<@Size(max = 40, message = "A tag is too long (max 40)") String> tags = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
