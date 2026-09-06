package com.restaurant.controller;

import com.restaurant.dto.ReviewAnalysisRequest;
import com.restaurant.dto.ReviewAnalysisResponse;
import com.restaurant.service.ReviewAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewAnalysisController {

    private final ReviewAnalysisService reviewAnalysisService;

    public ReviewAnalysisController(ReviewAnalysisService reviewAnalysisService) {
        this.reviewAnalysisService = reviewAnalysisService;
    }

    /**
     * Analyze restaurant reviews
     * POST /api/v1/reviews/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<ReviewAnalysisResponse> analyzeReviews(
            @RequestBody ReviewAnalysisRequest request) {

        log.info("Received analysis request for restaurant: {}", request.getRestaurantId());

        ReviewAnalysisResponse response = reviewAnalysisService.analyzeReviews(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Retrieve a previous analysis by ID
     * GET /api/v1/reviews/analysis/{analysisId}
     */
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<ReviewAnalysisResponse> getAnalysis(
            @PathVariable String analysisId) {

        log.info("Retrieving analysis: {}", analysisId);

        ReviewAnalysisResponse response = reviewAnalysisService.getAnalysis(analysisId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/reviews/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Review Analysis Service is running");
    }
}