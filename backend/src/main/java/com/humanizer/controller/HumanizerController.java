package com.humanizer.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizer.service.HumanizerService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow React frontend to access this API
public class HumanizerController {

    private final HumanizerService humanizerService;

    // Constructor Injection: Best practice for dependency injection
    @Autowired
    public HumanizerController(HumanizerService humanizerService) {
        this.humanizerService = humanizerService;
    }

    /**
     * Endpoint to humanize text.
     * Expects JSON: { "text": "some ai text", "level": "casual" }
     */
    @PostMapping("/humanize-text")
    public ResponseEntity<Map<String, String>> humanize(@RequestBody Map<String, String> request) {
        String originalText = request.get("text");
        String level = request.getOrDefault("level", "standard"); // Default to standard if missing
        
        // Call the service layer to process the text
        String humanizedText = humanizerService.humanizeText(originalText, level);
        
        // Return JSON response: { "humanized": "processed text" }
        return ResponseEntity.ok(Map.of("humanized", humanizedText));
    }
}
