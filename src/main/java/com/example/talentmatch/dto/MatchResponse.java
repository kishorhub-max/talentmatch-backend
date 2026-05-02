package com.example.talentmatch.dto;

import com.example.talentmatch.model.Resume;

public class MatchResponse {

    private ResumeSummary resume;
    private int score;

    // ✅ Constructor
    public MatchResponse(ResumeSummary resume, int score) {
        this.resume = resume;
        this.score = score;
    }

    // ✅ GETTERS (VERY IMPORTANT)
    public ResumeSummary getResume() {
        return resume;
    }

    public int getScore() {
        return score;
    }
}