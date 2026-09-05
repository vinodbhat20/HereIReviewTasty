package com.restaurant.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalysisRequest {
    private String restaurantId;
    private List<String> reviews;
}