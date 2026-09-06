package com.restaurant.repository;

import com.restaurant.domain.ReviewAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReviewAnalysisRepository extends JpaRepository<ReviewAnalysis, UUID> {
}