package com.example.restaurant.service;

import java.util.List;
import com.example.restaurant.dto.RestaurantDto;

public interface IRestaurantService {
    RestaurantDto createRestaurant(RestaurantDto restaurantDto);
    List<RestaurantDto> fetchRestaurants();
    RestaurantDto fetchRestaurant(Long id);
    boolean updateRestaurant(Long id, RestaurantDto restaurantDto);
    boolean deleteRestaurant(Long id);
}
