package com.restaurant.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(columnDefinition = "JSONB")
    private String analysisJson;

    @Column(columnDefinition = "JSONB")
    private String topDishes;

    @Column(columnDefinition = "JSONB")
    private String qualityRatings;

    @Column(columnDefinition = "text[]")
    private String[] concerns;

    @Column(columnDefinition = "text[]")
    private String[] praises;

    @Column
    private Double confidence;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}