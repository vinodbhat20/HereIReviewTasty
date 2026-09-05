package com.restaurant.repository;

import com.restaurant.domain.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {

    List<Dish> findByRestaurantId(UUID restaurantId);
}