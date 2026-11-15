package com.example.CRUD.controller;

import com.example.CRUD.dto.NlpAnalysisResult;
import com.example.CRUD.service.NlpModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nlp")
@RequiredArgsConstructor
public class NlpController {

    private final NlpModerationService nlpModerationService;

    @PostMapping("/analyze")
    public ResponseEntity<NlpAnalysisResult> analyze(@RequestBody Map<String, String> payload) {
        String text = payload.getOrDefault("text", "");
        return ResponseEntity.ok(nlpModerationService.analyze(text));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(nlpModerationService.getPlatformAnalytics());
    }

    @GetMapping("/samples")
    public ResponseEntity<List<Map<String, String>>> getSamples() {
        return ResponseEntity.ok(List.of(
                Map.of("category", "Positive", "text", "This project architecture is brilliant! Love the clean design ✨"),
                Map.of("category", "Neutral", "text", "The API responds in approximately 45 milliseconds."),
                Map.of("category", "Toxic (Should Block)", "text", "You are an idiot and this code is total garbage!")
        ));
    }
}
