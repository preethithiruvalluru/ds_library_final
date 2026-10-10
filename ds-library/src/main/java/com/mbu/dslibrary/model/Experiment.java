package com.mbu.dslibrary.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/** One lab experiment: aim, code, output and result. moduleId is an optional reference to a Module. */
@Document(collection = "experiments")
public class Experiment {
    @Id
    private String id;
    private String experimentNumber;
    private String title;
    private String aim;
    private String requirements;
    private String procedure;
    private String sourceCode;
    private String language;
    private String outputText;
    private StoredFile outputImage;
    private String result;
    private String moduleId;
    private String githubRepoUrl;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExperimentNumber() { return experimentNumber; }
    public void setExperimentNumber(String experimentNumber) { this.experimentNumber = experimentNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAim() { return aim; }
    public void setAim(String aim) { this.aim = aim; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getProcedure() { return procedure; }
    public void setProcedure(String procedure) { this.procedure = procedure; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getOutputText() { return outputText; }
    public void setOutputText(String outputText) { this.outputText = outputText; }
    public StoredFile getOutputImage() { return outputImage; }
    public void setOutputImage(StoredFile outputImage) { this.outputImage = outputImage; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getGithubRepoUrl() { return githubRepoUrl; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
