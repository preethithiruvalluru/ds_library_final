package com.mbu.dslibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** What the browser sends when creating or editing an experiment. */
public class ExperimentRequest {
    @NotBlank(message = "Experiment number is required")
    @Size(max = 20, message = "Experiment number is too long (max 20)")
    private String experimentNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long (max 200)")
    private String title;

    @Size(max = 10000, message = "Aim is too long")
    private String aim;

    @Size(max = 10000, message = "Requirements are too long")
    private String requirements;

    @Size(max = 20000, message = "Procedure is too long")
    private String procedure;

    @Size(max = 100000, message = "Source code is too long (max 100000 characters)")
    private String sourceCode;

    @Pattern(regexp = "python|java|c|cpp|javascript|r|sql|plaintext", message = "Unsupported programming language")
    private String language = "python";

    @Size(max = 20000, message = "Output text is too long")
    private String outputText;

    @Size(max = 10000, message = "Result is too long")
    private String result;

    private String moduleId;

    @Pattern(regexp = "^$|^https://github\\.com/[A-Za-z0-9_.-]+(/[A-Za-z0-9_.-]+)?(/[^\\s]*)?$",
            message = "GitHub URL must look like https://github.com/user/repo")
    @Size(max = 300, message = "GitHub URL is too long")
    private String githubRepoUrl;

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
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getGithubRepoUrl() { return githubRepoUrl; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }
}
