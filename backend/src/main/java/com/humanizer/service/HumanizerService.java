package com.humanizer.service;

import org.springframework.stereotype.Service;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HumanizerService {

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON parsing
    private final HttpClient httpClient = HttpClient.newHttpClient(); // java.net.http.HttpClient (Java 11+)

    /**
     * Core method to process text with a specific "humanization level".
     * Levels: "casual" (conversational), "standard" (balanced), "formal" (professional)
     */
    public String humanizeText(String input, String level) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 1. Sentence Breaking: Use OpenNLP to manage sentence structure
        String[] sentences = detectSentences(input);
        
        StringBuilder result = new StringBuilder();

        for (String sentence : sentences) {
             String processed = sentence;

             // 2. Apply strategy based on level
             if ("casual".equalsIgnoreCase(level)) {
                 processed = makeCasual(processed);
             } else if ("formal".equalsIgnoreCase(level)) {
                 processed = makeFormal(processed);
             } else if ("undetectable".equalsIgnoreCase(level)) {
                 processed = makeUndetectable(processed);
             } else {
                 // Standard: mix of both
                 processed = replaceContractions(processed);
                 processed = enhanceVocabulary(processed);
             }

             result.append(processed).append(" ");
        }

        return result.toString().trim();
    }

    /**
     * UNDETECTABLE MODE (The "200% Efficiency" request)
     * Strategies:
     * 1. Burstiness: Varying sentence length (splitting compound sentences).
     * 2. Perplexity: Using idioms and less predictable synonyms.
     * 3. Subjectivity: Injecting "human" opinions/hedging (e.g., "I think", "kinda").
     */
    private String makeUndetectable(String text) {
        String processed = text;

        // 1. Structural Variation (Burstiness)
        // AI often uses "comma and". Humans often just start a new sentence.
        processed = processed.replaceAll("(?i), and ", ". Plus, ");
        processed = processed.replaceAll("(?i), but ", ". But honestly, ");
        processed = processed.replaceAll("(?i), so ", ". So basically, ");

        // 2. Aggressive Idiom Injection
        processed = injectIdioms(processed);

        // 3. Mandatory Contractions
        processed = replaceContractions(processed);

        // 4. Human Noise/Hedging (Subjectivity)
        // AI states facts ("X is Y"). Humans hedge ("I feel like X is Y").
        processed = injectPersonality(processed);

        return processed;
    }

    /**
     * Replaces standard descriptors with more colorful, human idioms.
     */
    private String injectIdioms(String text) {
        Map<String, String> idioms = new HashMap<>();
        idioms.put("very important", "super key");
        idioms.put("difficult", "pretty tough");
        idioms.put("many", "a bunch of");
        idioms.put("good", "solid");
        idioms.put("bad", "lousy");
        idioms.put("happy", "thrilled");
        idioms.put("understand", "get");
        idioms.put("explain", "break down");
        idioms.put("necessary", "a must");
        idioms.put("therefore", "realistically");
        
        String result = text;
        for (Map.Entry<String, String> entry : idioms.entrySet()) {
             // Use word boundaries \\b to avoid replacing parts of words
             result = result.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return result;
    }

    /**
     * Injects "Voice" and "Tone" markers.
     */
    private String injectPersonality(String text) {
        // Randomly add qualifiers
        if (text.contains(" is ") && Math.random() > 0.6) {
            text = text.replaceFirst(" is ", " is, essentially, ");
        }
        
        // Randomly add intro markers if not already capitalized proper nouns (heuristic)
        if (Math.random() > 0.7 && !Character.isUpperCase(text.charAt(1))) { // Skip if second char is upper (e.g. iPhone)
             String[] intros = {"To be fair, ", "In my book, ", "Realistically, ", "I mean, "};
             int idx = (int) (Math.random() * intros.length);
             return intros[idx] + Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }
        return text;
    }

    /**
     * CASUAL MODE LOGIC
     * Focuses on shorter words, contractions, and conversational fillers.
     */
    private String makeCasual(String text) {
        // 1. Aggressive Contractions
        String processed = replaceContractions(text);

        // 2. Simplify complex words aggressively
        processed = enhanceVocabulary(processed);

        // 3. Conversational Transitions (Teaching: Randomness makes it feel less robotic)
        if (Math.random() > 0.7) { 
            processed = addConversationalStart(processed);
        }

        return processed;
    }

    /**
     * FORMAL MODE LOGIC
     * Focuses on correct grammar, avoids contractions, uses precise transitions.
     */
    private String makeFormal(String text) {
        // 1. Remove Contractions (Expand them back)
        // (For this simple version, we simply skip adding them)
        
        // 2. Add Formal Transitions
        if (Math.random() > 0.8 && !text.matches("^(However|Therefore|Additionally).*")) {
            processed = "Additionally, " + Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }

        return text; // Placeholder for now, can be expanded
    }

    /**
     * Adds natural conversation starters.
     * Why? AI text often dives straight into facts. Humans often hesitate or pave the way.
     */
    private String addConversationalStart(String text) {
        String[] starters = {"So, ", "You know, ", "Honestly, ", "Basically, "};
        int idx = (int) (Math.random() * starters.length);
        
        // Only add not already capitalized or specific starts
        if (Character.isUpperCase(text.charAt(0))) {
             return starters[idx] + Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }
        return text;
    }

    /**
     * INTERMEDIATE: Apache OpenNLP
     * Uses a pre-trained model to detect sentence boundaries intelligently.
     * Unlike simple period splitting, this handles "Mr." or "Dr." correctly.
     */
    private String[] detectSentences(String text) {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/en-sent.bin")) {
            if (modelIn != null) {
                SentenceModel model = new SentenceModel(modelIn);
                SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
                return sentenceDetector.sentDetect(text);
            }
        } catch (Exception e) {
            System.err.println("OpenNLP Model not found or error loading: " + e.getMessage());
            // Fallback to simple split if model fails
        }
        // Fallback: Split by period followed by space
        return text.split("(?<=[.!?])\\s+");
    }

    /**
     * INTERMEDIATE: External API Integration (Datamuse)
     * Replaces stiffness with better synonyms.
     */
    private String enhanceVocabulary(String text) {
        // List of words we want to "humanize"
        Map<String, String> complexWords = Map.of(
            "utilize", "use", 
            "demonstrate", "show",
            "facilitate", "help"
        );
        
        String result = text;
        
        // Check dynamically for synonyms if we want to expand beyond the static map
        // For this demo, let's say we want to find a better word for "moreover" if it exists
        if (result.toLowerCase().contains("moreover")) {
            String simpler = fetchSynonym("moreover");
            if (simpler != null) {
               result = result.replaceAll("(?i)moreover", simpler); 
            }
        }

        // Apply our static map (fast)
        for (Map.Entry<String, String> entry : complexWords.entrySet()) {
            result = result.replaceAll("(?i)" + entry.getKey(), entry.getValue());
        }
        
        return result;
    }

    /**
     * Connects to Datamuse API to find a synonym.
     * TEACHING: This shows how to make HTTP requests in Java.
     */
    private String fetchSynonym(String word) {
        try {
            // 1. Build the request
            String url = "https://api.datamuse.com/words?rel_syn=" + word + "&max=1";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // 2. Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Parse JSON response: [{"word":"also","score":123}]
            JsonNode root = objectMapper.readTree(response.body());
            
            if (root.isArray() && root.size() > 0) {
                return root.get(0).get("word").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if failure, keep original word
    }

    private String replaceContractions(String text) {
        // Map of formal phrases to contractions
        Map<String, String> contractions = new HashMap<>();
        contractions.put("do not", "don't");
        contractions.put("cannot", "can't");
        contractions.put("is not", "isn't");
        contractions.put("are not", "aren't");
        contractions.put("will not", "won't");
        contractions.put("I am", "I'm");
        contractions.put("it is", "it's");
        
        String result = text;
        for (Map.Entry<String, String> entry : contractions.entrySet()) {
            // Case insensitive replacement using regex
            result = result.replaceAll("(?i)" + entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String simplifyLanguage(String text) {
        // Example of simplifying stiff language
        return text.replaceAll("(?i)utilize", "use")
                   .replaceAll("(?i)facilitate", "help")
                   .replaceAll("(?i)additionally", "also")
                   .replaceAll("(?i)furthermore", "also");
    }
}
