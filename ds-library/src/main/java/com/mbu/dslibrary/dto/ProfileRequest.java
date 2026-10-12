package com.mbu.dslibrary.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** What the browser sends when saving the profile. Every field is optional so you can fill it in gradually. */
public class ProfileRequest {
    @Size(max = 100, message = "Name is too long (max 100)")
    private String name;

    @Size(max = 40, message = "Roll number is too long (max 40)")
    private String rollNumber;

    @Size(max = 100, message = "Department is too long (max 100)")
    private String department;

    @Size(max = 20, message = "Section is too long (max 20)")
    private String section;

    @Size(max = 100, message = "Program is too long (max 100)")
    private String program;

    @Size(max = 30, message = "Academic year is too long (max 30)")
    private String academicYear;

    @Size(max = 30, message = "Semester is too long (max 30)")
    private String semester;

    @Size(max = 150, message = "College name is too long (max 150)")
    private String college;

    @Size(max = 100, message = "Mentor name is too long (max 100)")
    private String mentorName;

    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Email is not valid")
    @Size(max = 120, message = "Email is too long")
    private String email;

    @Pattern(regexp = "^$|^https://github\\.com/[A-Za-z0-9_.-]+/?$", message = "GitHub profile must look like https://github.com/username")
    private String githubUrl;

    @Pattern(regexp = "^$|^https://([a-z]{2,3}\\.)?linkedin\\.com/[A-Za-z0-9_%./-]+$", message = "LinkedIn URL must look like https://www.linkedin.com/in/username")
    @Size(max = 200, message = "LinkedIn URL is too long")
    private String linkedinUrl;

    @Size(max = 30, message = "Use at most 30 skills")
    private List<@Size(max = 40, message = "A skill name is too long (max 40)") String> skills = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getMentorName() { return mentorName; }
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
