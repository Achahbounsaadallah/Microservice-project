package com.example.restaurant.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.restaurant.entity.Meal;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByRestaurantId(Long restaurantId);
    List<Meal> findByRestaurantIdAndAvailableTrue(Long restaurantId);
}
