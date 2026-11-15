package com.example.CRUD.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NlpAnalysisResult {
    private String text;
    private boolean isToxic;
    private double toxicityScore;
    private boolean isNsfw;
    private double nsfwScore;
    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
    private double sentimentScore;
    private String explanation;
    private String modelSource;
}
