package com.example.talentmatch.dto;

public class ResumeSummary {

    private Long id;
    private String name;
    private String email;
    private String skills;
    private int experience;

    public ResumeSummary(Long id, String name, String email, String skills, int experience) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.experience = experience;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSkills() { return skills; }
    public int getExperience() { return experience; }
}