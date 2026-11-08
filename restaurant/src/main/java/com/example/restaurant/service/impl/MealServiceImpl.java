package com.example.restaurant.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.restaurant.dto.MealDto;
import com.example.restaurant.entity.Meal;
import com.example.restaurant.entity.Restaurant;
import com.example.restaurant.exception.ResourceNotFoundException;
import com.example.restaurant.mapper.MealMapper;
import com.example.restaurant.repository.MealRepository;
import com.example.restaurant.repository.RestaurantRepository;
import com.example.restaurant.service.IMealService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MealServiceImpl implements IMealService {
    private final MealRepository mealRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public MealDto createMeal(Long restaurantId, MealDto dto) {
        Restaurant restaurant = findRestaurant(restaurantId);
        Meal meal = MealMapper.mapToMeal(dto, new Meal());
        meal.setRestaurant(restaurant);
        return MealMapper.mapToMealDto(mealRepository.save(meal), new MealDto());
    }

    @Override
    public List<MealDto> fetchMeals(Long restaurantId) {
        findRestaurant(restaurantId);
        return mealRepository.findByRestaurantId(restaurantId).stream()
                .map(meal -> MealMapper.mapToMealDto(meal, new MealDto())).toList();
    }

    @Override
    public MealDto fetchMeal(Long id) { return MealMapper.mapToMealDto(findMeal(id), new MealDto()); }

    @Override
    @Transactional
    public boolean updateMeal(Long id, MealDto dto) {
        Meal meal = findMeal(id);
        MealMapper.mapToMeal(dto, meal);
        mealRepository.save(meal);
        return true;
    }

    @Override
    public boolean deleteMeal(Long id) {
        mealRepository.delete(findMeal(id));
        return true;
    }

    @Override
    @Transactional
    public boolean updateAvailability(Long id, boolean available) {
        Meal meal = findMeal(id);
        meal.setAvailable(available);
        mealRepository.save(meal);
        return true;
    }

    @Override
    @Transactional
    public boolean updatePrice(Long id, BigDecimal price) {
        Meal meal = findMeal(id);
        meal.setPrice(price);
        mealRepository.save(meal);
        return true;
    }

    private Meal findMeal(Long id) {
        return mealRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meal", "id", id.toString()));
    }
    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id.toString()));
    }
}
