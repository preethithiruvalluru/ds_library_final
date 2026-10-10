package com.mbu.dslibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** What the browser sends when creating or editing a tool. */
public class ToolRequest {
    @NotBlank(message = "Tool name is required")
    @Size(max = 80, message = "Name is too long (max 80)")
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 60, message = "Category is too long (max 60)")
    private String category;

    @Size(max = 500, message = "Description is too long (max 500)")
    private String description;

    @NotBlank(message = "Official website is required")
    @Pattern(regexp = "^https?://[^\\s]+$", message = "Website must start with http:// or https://")
    @Size(max = 500, message = "Website link is too long")
    private String officialUrl;

    @Pattern(regexp = "^$|^bi-[a-z0-9-]+$", message = "Icon must be a Bootstrap icon class like bi-database")
    private String icon;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
