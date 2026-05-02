package com.example.talentmatch.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ResumeParserService {

    public String extractText(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PDF");
        }
    }

    public String extractSkills(String text) {

        List<String> skillsList = Arrays.asList(
                "java", "spring", "spring boot", "hibernate",
                "mysql", "postgresql", "docker", "aws",
                "react", "angular", "javascript",
                "python", "microservices", "rest api"
        );

        text = text.toLowerCase();
        Set<String> foundSkills = new HashSet<>();

        for (String skill : skillsList) {
            if (text.contains(skill)) {
                foundSkills.add(skill);
            }
        }

        return String.join(",", foundSkills);
    }
}