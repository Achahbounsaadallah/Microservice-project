package com.example.restaurant.service;

import java.math.BigDecimal;
import java.util.List;
import com.example.restaurant.dto.MealDto;

public interface IMealService {
    MealDto createMeal(Long restaurantId, MealDto mealDto);
    List<MealDto> fetchMeals(Long restaurantId);
    MealDto fetchMeal(Long id);
    boolean updateMeal(Long id, MealDto mealDto);
    boolean deleteMeal(Long id);
    boolean updateAvailability(Long id, boolean available);
    boolean updatePrice(Long id, BigDecimal price);
}
