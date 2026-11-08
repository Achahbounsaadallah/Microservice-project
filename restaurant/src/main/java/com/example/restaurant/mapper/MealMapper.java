package com.example.restaurant.mapper;

import com.example.restaurant.dto.MealDto;
import com.example.restaurant.entity.Meal;

public final class MealMapper {
    private MealMapper() { }
    public static MealDto mapToMealDto(Meal meal, MealDto dto) {
        dto.setId(meal.getId());
        dto.setName(meal.getName());
        dto.setDescription(meal.getDescription());
        dto.setPrice(meal.getPrice());
        dto.setAvailable(meal.isAvailable());
        dto.setRestaurantId(meal.getRestaurant().getId());
        return dto;
    }
    public static Meal mapToMeal(MealDto dto, Meal meal) {
        meal.setName(dto.getName());
        meal.setDescription(dto.getDescription());
        meal.setPrice(dto.getPrice());
        meal.setAvailable(dto.isAvailable());
        return meal;
    }
}
