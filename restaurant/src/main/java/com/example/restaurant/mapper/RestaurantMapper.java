package com.example.restaurant.mapper;

import com.example.restaurant.dto.RestaurantDto;
import com.example.restaurant.entity.Restaurant;

public final class RestaurantMapper {
    private RestaurantMapper() { }
    public static RestaurantDto mapToRestaurantDto(Restaurant restaurant, RestaurantDto dto) {
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setOpen(restaurant.isOpen());
        return dto;
    }
    public static Restaurant mapToRestaurant(RestaurantDto dto, Restaurant restaurant) {
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setOpen(dto.isOpen());
        return restaurant;
    }
}
