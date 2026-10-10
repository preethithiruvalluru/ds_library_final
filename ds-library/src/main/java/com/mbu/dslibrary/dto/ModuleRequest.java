package com.mbu.dslibrary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** What the browser sends when creating or editing a module. */
public class ModuleRequest {
    @Min(value = 1, message = "Module number must be 1 or more")
    private int moduleNumber;

    @NotBlank(message = "Module name is required")
    @Size(max = 150, message = "Name is too long (max 150)")
    private String name;

    @NotBlank(message = "Subject is required")
    @Size(max = 150, message = "Subject is too long (max 150)")
    private String subject;

    @Size(max = 2000, message = "Description is too long (max 2000)")
    private String description;

    public int getModuleNumber() { return moduleNumber; }
    public void setModuleNumber(int moduleNumber) { this.moduleNumber = moduleNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
