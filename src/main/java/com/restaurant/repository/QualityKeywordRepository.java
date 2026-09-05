package com.restaurant.repository;

import com.restaurant.domain.QualityKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualityKeywordRepository extends JpaRepository<QualityKeyword, UUID> {
    List<QualityKeyword> findByAspectAndSentiment(String aspect, String sentiment);
}