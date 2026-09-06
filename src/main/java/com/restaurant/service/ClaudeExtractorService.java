package com.restaurant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.mcp.RestaurantMcpTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class ClaudeExtractorService {

    private final ChatLanguageModel chatLanguageModel;
    private final RestaurantMcpTools mcpTools;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeExtractorService(
            @Value("${claude.api.key}") String apiKey,
            @Value("${claude.model}") String model,
            @Value("${claude.max-tokens}") int maxTokens,
            RestaurantMcpTools mcpTools) {

        // Initialize Claude model with LangChain4j
        this.chatLanguageModel = AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .maxTokens(maxTokens)
                .build();

        this.mcpTools = mcpTools;
        this.model = model;
    }

    /**
     * Extract structured data from reviews using Claude
     * Claude will call MCP tools to get restaurant context
     */
    public ReviewExtractionResult extractFromReviews(String restaurantId, List<String> reviews) {
        try {
            long startTime = System.currentTimeMillis();

            // Build the prompt for Claude
            String prompt = buildExtractionPrompt(restaurantId, reviews);

            log.info("Sending {} reviews to Claude for analysis", reviews.size());

            // Call Claude (this is where the magic happens)
            // Claude will see the MCP tools are available and may call them
            String response = chatLanguageModel.generate(prompt);

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("Claude analysis completed in {}ms", processingTime);

            // Parse Claude's JSON response
            return parseClaudeResponse(response, processingTime, restaurantId);

        } catch (Exception e) {
            log.error("Error extracting review data", e);
            throw new RuntimeException("Failed to extract review data: " + e.getMessage(), e);
        }
    }

    /**
     * Build the prompt that Claude will see
     * This tells Claude what tools are available and what to do
     */
    private String buildExtractionPrompt(String restaurantId, List<String> reviews) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a restaurant review analysis expert.\n\n");
        sb.append("You have access to these tools:\n");
        sb.append("1. getRestaurantMenu(restaurantId) - Get the menu for a restaurant\n");
        sb.append("2. getQualityKeywords() - Get positive/negative keywords for sentiment analysis\n\n");

        sb.append("TASK: Analyze the following reviews and extract:\n");
        sb.append("- Top mentioned dishes\n");
        sb.append("- Quality ratings by aspect (food, service, cleanliness, ambiance)\n");
        sb.append("- Key concerns and praises\n");
        sb.append("- Confidence score (0.0 to 1.0)\n\n");

        sb.append("Restaurant ID: ").append(restaurantId).append("\n\n");

        sb.append("Reviews to analyze:\n");
        for (int i = 0; i < reviews.size(); i++) {
            sb.append(i + 1).append(". ").append(reviews.get(i)).append("\n");
        }

        sb.append("\nReturn ONLY valid JSON (no markdown, no explanation):\n");
        sb.append("{\n");
        sb.append("  \"top_dishes\": [\n");
        sb.append("    {\"name\": \"...\", \"mentions\": 1, \"avg_rating\": 4.5, \"sentiment\": \"positive\"}\n");
        sb.append("  ],\n");
        sb.append("  \"quality_aspects\": {\n");
        sb.append("    \"food_quality\": {\"rating\": 4.5, \"keywords\": [\"amazing\", \"fresh\"]},\n");
        sb.append("    \"service\": {\"rating\": 3.0, \"keywords\": [\"slow\"]},\n");
        sb.append("    \"cleanliness\": {\"rating\": 4.0, \"keywords\": []},\n");
        sb.append("    \"ambiance\": {\"rating\": 3.5, \"keywords\": []}\n");
        sb.append("  },\n");
        sb.append("  \"key_concerns\": [\"...\"],\n");
        sb.append("  \"key_praises\": [\"...\"],\n");
        sb.append("  \"confidence\": 0.92\n");
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Parse Claude's JSON response into structured data
     */
    private ReviewExtractionResult parseClaudeResponse(
            String response,
            long processingTime,
            String restaurantId) throws Exception {

        // Claude should return valid JSON
        JsonNode jsonNode = objectMapper.readTree(response);

        return ReviewExtractionResult.builder()
                .restaurantId(restaurantId)
                .topDishes(objectMapper.writeValueAsString(jsonNode.get("top_dishes")))
                .qualityAspects(objectMapper.writeValueAsString(jsonNode.get("quality_aspects")))
                .keyConcerns(extractArrayFromJson(jsonNode, "key_concerns"))
                .keyPraises(extractArrayFromJson(jsonNode, "key_praises"))
                .confidence(jsonNode.get("confidence").asDouble())
                .modelUsed(model)
                .processingTimeMs((int) processingTime)
                .rawAnalysis(response)
                .build();
    }

    /**
     * Extract string array from JSON node
     */
    private String[] extractArrayFromJson(JsonNode node, String fieldName) {
        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode == null || !arrayNode.isArray()) {
            return new String[0];
        }

        List<String> list = new ArrayList<>();
        arrayNode.forEach(item -> list.add(item.asText()));
        return list.toArray(new String[0]);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReviewExtractionResult {
        private String restaurantId;
        private String topDishes;
        private String qualityAspects;
        private String[] keyConcerns;
        private String[] keyPraises;
        private Double confidence;
        private String modelUsed;
        private Integer processingTimeMs;
        private String rawAnalysis;
    }
}