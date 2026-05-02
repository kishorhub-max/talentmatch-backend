package com.example.talentmatch.controller;
import com.example.talentmatch.service.ResumeParserService;
import com.example.talentmatch.dto.MatchResponse;
import com.example.talentmatch.dto.ResumeSummary;
import com.example.talentmatch.model.Resume;
import com.example.talentmatch.model.User;
import com.example.talentmatch.repository.ResumeRepository;
import com.example.talentmatch.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/resumes")
@CrossOrigin(origins = "http://localhost:3000")
public class ResumeController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResumeParserService parserService;


    private final ResumeRepository resumeRepository;

    public ResumeController(ResumeRepository repository) {
        this.resumeRepository = repository;
    }

    // ✅ GET BY ID (with security)
    @GetMapping("/{id}")
    public Resume getResumeById(@PathVariable Long id, Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName();

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        return resume;
    }

    // ✅ UPDATE (with security)
    @PutMapping("/{id}")
    public Resume updateResume(@PathVariable Long id,
                               @RequestBody Resume updatedResume,
                               Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName();

        return resumeRepository.findById(id).map(resume -> {

            if (!resume.getUser().getUsername().equals(username)) {
                throw new RuntimeException("Access denied");
            }

            resume.setName(updatedResume.getName());
            resume.setEmail(updatedResume.getEmail());
            resume.setSkills(updatedResume.getSkills());
            resume.setExperience(updatedResume.getExperience());

            return resumeRepository.save(resume);

        }).orElseThrow(() -> new RuntimeException("Resume not found"));
    }

    // ✅ DELETE (with security)
    @DeleteMapping("/{id}")
    public String deleteResume(@PathVariable Long id, Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName();
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        resumeRepository.deleteById(id);
        return "Deleted successfully";
    }

    // ✅ GET ALL (ADMIN vs USER)
    @GetMapping
    public List<Resume> getResumes(Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return resumeRepository.findAll();
        }

        return resumeRepository.findByUserUsername(username);
    }
    @GetMapping("/test")
    public String test() {
        return "Backend is working!";
    }

    // ✅ SEARCH

    @GetMapping("/search")
    public List<Resume> searchBySkill(@RequestParam String skill) {
        return resumeRepository.findBySkillsContaining(skill);
    }

    // 🔥 MATCH API (FINAL VERSION)
    @GetMapping("/match")
    public List<MatchResponse> matchResumes(
            @RequestParam String skill,
            @RequestParam(required = false) Integer experience
    ) {

        List<Resume> resumes = resumeRepository.findAll();

        return resumes.stream()
                .filter(r -> experience == null || r.getExperience() >= experience)
                .map(r -> new MatchResponse(
                        convertToSummary(r),
                        calculateScore(r, skill, experience != null ? experience : 0)
                ))
                .sorted((a, b) -> b.getScore() - a.getScore())
                .limit(5)
                .toList();
    }

    // 🔥 TOP CANDIDATES (DYNAMIC)
    @GetMapping("/top-candidates")
    public List<MatchResponse> getTopCandidates(
            @RequestParam(defaultValue = "java spring boot") String skill
    ) {

        return resumeRepository.findAll().stream()
                .map(r -> new MatchResponse(
                        convertToSummary(r),
                        calculateScore(r, skill, 0)
                ))
                .sorted((a, b) -> b.getScore() - a.getScore())
                .limit(3)
                .toList();
    }

    // ✅ CREATE
    @PostMapping
    public Resume createResume(@RequestBody Resume resume, Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return resumeRepository.save(resume);
    }

    // ✅ UPLOAD FILE
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Resume uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String skills, // optional now
            @RequestParam int experience,
            Authentication authentication
    ) throws Exception {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
//        User user = userRepository.findAll()
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("No user found"));

        Resume resume = new Resume();
                resume.setName(name);
        resume.setEmail(email);

        // 🔥 STEP 1: Extract text from PDF
        String text = parserService.extractText(file.getInputStream());

        // 🔥 STEP 2: Extract skills automatically
        String extractedSkills = parserService.extractSkills(text);

        // 🔥 STEP 3: Merge manual + auto skills (optional)
        if (skills != null && !skills.isEmpty()) {
            extractedSkills = extractedSkills + "," + skills;
        }

        // ✅ FINAL SKILLS
        resume.setSkills(extractedSkills);

        // ✅ EXPERIENCE (int)
        resume.setExperience(experience);

        // 📄 FILE DATA
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setFileData(file.getBytes());

        resume.setUser(user);

        return resumeRepository.save(resume);
    }

    // ✅ DOWNLOAD
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + resume.getFileName() + "\"")
                .body(resume.getFileData());
    }

    // 🔥 CONVERT TO DTO (IMPORTANT)
    private ResumeSummary convertToSummary(Resume r) {
        return new ResumeSummary(
                r.getId(),
                r.getName(),
                r.getEmail(),
                r.getSkills(),
                r.getExperience()
        );
    }

    // 🔥 FINAL SCORING LOGIC
    private int calculateScore(Resume resume, String skill, int experience) {

        int score = 0;

        // 🔥 1. MULTI-SKILL MATCH (50 points)
        String[] requiredSkills = skill.toLowerCase().split(" ");
        String resumeSkills = resume.getSkills().toLowerCase();

        int matchCount = 0;

        for (String s : requiredSkills) {
            if (resumeSkills.contains(s)) {
                matchCount++;
            }
        }

        // percentage match
        int skillScore = (matchCount * 50) / requiredSkills.length;
        score += skillScore;

        // 🔥 2. EXPERIENCE SCORE (30 points)
        if (experience > 0) {
            int exp = resume.getExperience();

            if (exp >= experience) {
                score += 30;
            } else {
                score += (exp * 30) / experience;
            }
        }

        // 🔥 3. BONUS KEYWORDS (20 points)
        if (resumeSkills.contains("spring")) score += 10;
        if (resumeSkills.contains("microservices")) score += 5;
        if (resumeSkills.contains("aws")) score += 5;

        return score;
    }}