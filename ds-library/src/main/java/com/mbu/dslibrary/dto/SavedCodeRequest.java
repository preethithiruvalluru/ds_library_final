package com.mbu.dslibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** What the browser sends when saving code from the editor. */
public class SavedCodeRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title is too long (max 120)")
    private String title;

    @NotBlank(message = "Language is required")
    @Pattern(regexp = "java|python|c|cpp|javascript|r|sql", message = "Unsupported programming language")
    private String language;

    @Size(max = 100000, message = "Code is too long (max 100000 characters)")
    private String code = "";

    @Size(max = 10000, message = "Input is too long (max 10000 characters)")
    private String stdin = "";

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStdin() { return stdin; }
    public void setStdin(String stdin) { this.stdin = stdin; }
}
