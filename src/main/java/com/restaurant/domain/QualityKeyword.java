package com.restaurant.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quality_keywords")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String aspect;  // 'food', 'service', 'cleanliness', 'ambiance'

    @Column(nullable = false)
    private String sentiment;  // 'positive', 'negative'

    @Column(nullable = false)
    private String keyword;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}