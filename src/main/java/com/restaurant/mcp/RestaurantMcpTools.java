package com.restaurant.mcp;

import com.restaurant.domain.Dish;
import com.restaurant.domain.QualityKeyword;
import com.restaurant.repository.DishRepository;
import com.restaurant.repository.QualityKeywordRepository;
import dev.langchain4j.agent.tool.Tool;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Getter
@Setter
@AllArgsConstructor
public class RestaurantMcpTools {

    private DishRepository dishRepository;
    private QualityKeywordRepository qualityKeywordRepository;

    /**
     * MCP Tool: Get menu for a restaurant
     * Claude calls this when it needs to see what dishes exist
     */
    @Tool("Get the menu for a restaurant")
    public MenuResponse getRestaurantMenu(String restaurantId) {
        UUID restaurantUUID = UUID.fromString(restaurantId);
        List<Dish> dishes = dishRepository.findByRestaurantId(restaurantUUID);

        List<DishInfo> dishList = dishes.stream()
                .map(dish -> DishInfo.builder()
                        .name(dish.getName())
                        .description(dish.getDescription())
                        .price(dish.getPrice())
                        .build())
                .collect(Collectors.toList());

        return MenuResponse.builder()
                .restaurantId(restaurantId)
                .dishes(dishList)
                .count(dishList.size())
                .build();
    }

    /**
     * MCP Tool: Get quality keywords for sentiment analysis
     * Claude calls this to understand positive/negative keywords per aspect
     */
    @Tool("Get quality keywords for analyzing reviews by aspect")
    public QualityKeywordOntology getQualityKeywords() {
        Map<String, QualityKeywordsByAspect> ontology = new HashMap<>();

        // Group keywords by aspect
        List<String> aspects = Arrays.asList("food", "service", "cleanliness", "ambiance");

        for (String aspect : aspects) {
            List<QualityKeyword> positiveKeywords = qualityKeywordRepository
                    .findByAspectAndSentiment(aspect, "positive");
            List<QualityKeyword> negativeKeywords = qualityKeywordRepository
                    .findByAspectAndSentiment(aspect, "negative");

            List<String> positive = positiveKeywords.stream()
                    .map(QualityKeyword::getKeyword)
                    .collect(Collectors.toList());
            List<String> negative = negativeKeywords.stream()
                    .map(QualityKeyword::getKeyword)
                    .collect(Collectors.toList());

            ontology.put(aspect, QualityKeywordsByAspect.builder()
                    .positive(positive)
                    .negative(negative)
                    .build());
        }

        return QualityKeywordOntology.builder()
                .keywords(ontology)
                .aspects(new ArrayList<>(ontology.keySet()))
                .build();
    }

    // DTOs for MCP responses

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuResponse {
        private String restaurantId;
        private List<DishInfo> dishes;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DishInfo {
        private String name;
        private String description;
        private java.math.BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityKeywordOntology {
        private Map<String, QualityKeywordsByAspect> keywords;
        private List<String> aspects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityKeywordsByAspect {
        private List<String> positive;
        private List<String> negative;
    }
}