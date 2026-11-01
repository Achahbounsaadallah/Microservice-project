package com.example.restaurant.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.restaurant.dto.RestaurantDto;
import com.example.restaurant.entity.Restaurant;
import com.example.restaurant.exception.ResourceNotFoundException;
import com.example.restaurant.exception.RestaurantAlreadyExistsException;
import com.example.restaurant.mapper.RestaurantMapper;
import com.example.restaurant.repository.RestaurantRepository;
import com.example.restaurant.service.IRestaurantService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RestaurantServiceImpl implements IRestaurantService {
    private final RestaurantRepository restaurantRepository;

    @Override
    public RestaurantDto createRestaurant(RestaurantDto dto) {
        if (restaurantRepository.existsByNameIgnoreCase(dto.getName()))
            throw new RestaurantAlreadyExistsException("Restaurant already exists with name " + dto.getName());
        Restaurant saved = restaurantRepository.save(RestaurantMapper.mapToRestaurant(dto, new Restaurant()));
        return RestaurantMapper.mapToRestaurantDto(saved, new RestaurantDto());
    }

    @Override
    public List<RestaurantDto> fetchRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(restaurant -> RestaurantMapper.mapToRestaurantDto(restaurant, new RestaurantDto())).toList();
    }

    @Override
    public RestaurantDto fetchRestaurant(Long id) {
        Restaurant restaurant = findRestaurant(id);
        return RestaurantMapper.mapToRestaurantDto(restaurant, new RestaurantDto());
    }

    @Override
    @Transactional
    public boolean updateRestaurant(Long id, RestaurantDto dto) {
        Restaurant restaurant = findRestaurant(id);
        RestaurantMapper.mapToRestaurant(dto, restaurant);
        restaurantRepository.save(restaurant);
        return true;
    }

    @Override
    public boolean deleteRestaurant(Long id) {
        Restaurant restaurant = findRestaurant(id);
        restaurantRepository.delete(restaurant);
        return true;
    }

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Restaurant", "id", id.toString()));
    }
}
