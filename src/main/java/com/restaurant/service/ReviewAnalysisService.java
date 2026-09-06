package com.restaurant.service;

import com.restaurant.domain.ReviewAnalysis;
import com.restaurant.dto.ReviewAnalysisRequest;
import com.restaurant.dto.ReviewAnalysisResponse;
import com.restaurant.repository.ReviewAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class ReviewAnalysisService {

    private final ClaudeExtractorService claudeExtractorService;
    private final ReviewAnalysisRepository reviewAnalysisRepository;

    public ReviewAnalysisService(
            ClaudeExtractorService claudeExtractorService,
            ReviewAnalysisRepository reviewAnalysisRepository) {
        this.claudeExtractorService = claudeExtractorService;
        this.reviewAnalysisRepository = reviewAnalysisRepository;
    }

    /**
     * Main orchestration method
     * 1. Call Claude to extract data from reviews
     * 2. Store results in database
     * 3. Return response
     */
    public ReviewAnalysisResponse analyzeReviews(ReviewAnalysisRequest request) {
        try {
            log.info("Starting review analysis for restaurant: {}", request.getRestaurantId());

            // Validate input
            if (request.getReviews() == null || request.getReviews().isEmpty()) {
                return ReviewAnalysisResponse.error("No reviews provided");
            }

            if (request.getRestaurantId() == null || request.getRestaurantId().isEmpty()) {
                return ReviewAnalysisResponse.error("Restaurant ID is required");
            }

            // Step 1: Call Claude to extract data from reviews
            log.debug("Calling Claude extractor service...");
            ClaudeExtractorService.ReviewExtractionResult extractionResult =
                    claudeExtractorService.extractFromReviews(
                            request.getRestaurantId(),
                            request.getReviews()
                    );

            // Step 2: Store results in database
            log.debug("Storing analysis results in database...");
            ReviewAnalysis analysis = ReviewAnalysis.builder()
                    .restaurantId(UUID.fromString(request.getRestaurantId()))
                    .analysisJson(extractionResult.getRawAnalysis())
                    .topDishes(extractionResult.getTopDishes())
                    .qualityRatings(extractionResult.getQualityAspects())
                    .concerns(extractionResult.getKeyConcerns())
                    .praises(extractionResult.getKeyPraises())
                    .confidence(extractionResult.getConfidence())
                    .modelUsed(extractionResult.getModelUsed())
                    .processingTimeMs(extractionResult.getProcessingTimeMs())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            ReviewAnalysis savedAnalysis = reviewAnalysisRepository.save(analysis);

            log.info("Review analysis completed successfully. Analysis ID: {}", savedAnalysis.getId());

            // Step 3: Return response
            return ReviewAnalysisResponse.fromEntity(savedAnalysis);

        } catch (Exception e) {
            log.error("Error during review analysis", e);
            return ReviewAnalysisResponse.error("Analysis failed: " + e.getMessage());
        }
    }

    /**
     * Retrieve a previous analysis by ID
     */
    public ReviewAnalysisResponse getAnalysis(String analysisId) {
        try {
            ReviewAnalysis analysis = reviewAnalysisRepository.findById(UUID.fromString(analysisId))
                    .orElse(null);

            if (analysis == null) {
                return ReviewAnalysisResponse.error("Analysis not found");
            }

            return ReviewAnalysisResponse.fromEntity(analysis);
        } catch (Exception e) {
            log.error("Error retrieving analysis", e);
            return ReviewAnalysisResponse.error("Failed to retrieve analysis: " + e.getMessage());
        }
    }
}