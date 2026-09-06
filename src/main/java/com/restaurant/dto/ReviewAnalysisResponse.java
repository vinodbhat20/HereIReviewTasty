package com.restaurant.dto;

import com.restaurant.domain.ReviewAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalysisResponse {
    private boolean success;
    private String analysisId;
    private String restaurantId;
    private Map<String, Object> topDishes;
    private Map<String, Object> qualityRatings;
    private List<String> concerns;
    private List<String> praises;
    private Double confidence;
    private Integer processingTimeMs;
    private String errorMessage;

    /**
     * Convert ReviewAnalysis entity to DTO response
     */
    public static ReviewAnalysisResponse fromEntity(ReviewAnalysis entity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> topDishes = entity.getTopDishes() != null ?
                    objectMapper.readValue(entity.getTopDishes(), Map.class) : null;

            Map<String, Object> qualityRatings = entity.getQualityRatings() != null ?
                    objectMapper.readValue(entity.getQualityRatings(), Map.class) : null;

            return ReviewAnalysisResponse.builder()
                    .success(true)
                    .analysisId(entity.getId().toString())
                    .restaurantId(entity.getRestaurantId().toString())
                    .topDishes(topDishes)
                    .qualityRatings(qualityRatings)
                    .concerns(entity.getConcerns() != null ?
                            Arrays.asList(entity.getConcerns()) : null)
                    .praises(entity.getPraises() != null ?
                            Arrays.asList(entity.getPraises()) : null)
                    .confidence(entity.getConfidence())
                    .processingTimeMs(entity.getProcessingTimeMs())
                    .build();
        } catch (Exception e) {
            return ReviewAnalysisResponse.builder()
                    .success(false)
                    .errorMessage("Failed to parse analysis: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Create error response
     */
    public static ReviewAnalysisResponse error(String message) {
        return ReviewAnalysisResponse.builder()
                .success(false)
                .errorMessage(message)
                .build();
    }
}