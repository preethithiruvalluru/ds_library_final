package com.mbu.dslibrary.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/** A Data Science tool shown as a card in the Tools directory. */
@Document(collection = "tools")
public class Tool {
    @Id
    private String id;
    private String name;
    private String category;
    private String description;
    private String officialUrl;
    /** Optional Bootstrap Icons class such as "bi-database". Empty means "use the category icon". */
    private String icon;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public Tool() {}

    public Tool(String name, String category, String description, String officialUrl, String icon) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.officialUrl = officialUrl;
        this.icon = icon;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
