package com.example.restaurant.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.restaurant.constants.RestaurantConstants;
import com.example.restaurant.dto.ErrorResponseDto;
import com.example.restaurant.dto.MealDto;
import com.example.restaurant.dto.ResponseDto;
import com.example.restaurant.service.IMealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Tag(name = "Meals", description = "Restaurant meal and menu APIs")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class MealController {
    private final IMealService mealService;
    public MealController(IMealService mealService) { this.mealService = mealService; }

    @Operation(summary = "Create a meal for a restaurant")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Created"), @ApiResponse(responseCode = "400", description = "Validation error"), @ApiResponse(responseCode = "404", description = "Restaurant not found")})
    @PostMapping(path = "/restaurants/{id}/meals", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MealDto> createMeal(@PathVariable Long id, @Valid @RequestBody MealDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealService.createMeal(id, dto));
    }

    @Operation(summary = "List meals for a restaurant")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meals returned"), @ApiResponse(responseCode = "404", description = "Restaurant not found")})
    @GetMapping("/restaurants/{id}/meals")
    public ResponseEntity<List<MealDto>> fetchMeals(@PathVariable Long id) { return ResponseEntity.ok(mealService.fetchMeals(id)); }

    @Operation(summary = "Get meal price and availability")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meal returned"), @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponseDto.class)))})
    @GetMapping("/meals/{id}")
    public ResponseEntity<MealDto> fetchMeal(@PathVariable Long id) { return ResponseEntity.ok(mealService.fetchMeal(id)); }

    @Operation(summary = "Update a meal")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated"), @ApiResponse(responseCode = "400", description = "Validation error"), @ApiResponse(responseCode = "404", description = "Not found")})
    @PutMapping(path = "/meals/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> updateMeal(@PathVariable Long id, @Valid @RequestBody MealDto dto) {
        mealService.updateMeal(id, dto);
        return ResponseEntity.ok(new ResponseDto(RestaurantConstants.STATUS_200, RestaurantConstants.MESSAGE_200));
    }

    @Operation(summary = "Delete a meal")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted"), @ApiResponse(responseCode = "404", description = "Not found")})
    @DeleteMapping("/meals/{id}")
    public ResponseEntity<ResponseDto> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.ok(new ResponseDto(RestaurantConstants.STATUS_200, RestaurantConstants.MESSAGE_200));
    }

    @Operation(summary = "Change meal availability")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Availability updated"), @ApiResponse(responseCode = "404", description = "Not found")})
    @PatchMapping("/meals/{id}/availability")
    public ResponseEntity<ResponseDto> updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        mealService.updateAvailability(id, available);
        return ResponseEntity.ok(new ResponseDto(RestaurantConstants.STATUS_200, RestaurantConstants.MESSAGE_200));
    }

    @Operation(summary = "Change meal price")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Price updated"), @ApiResponse(responseCode = "400", description = "Invalid price"), @ApiResponse(responseCode = "404", description = "Not found")})
    @PatchMapping("/meals/{id}/price")
    public ResponseEntity<ResponseDto> updatePrice(@PathVariable Long id, @RequestParam @NotNull @DecimalMin("0.0") BigDecimal price) {
        mealService.updatePrice(id, price);
        return ResponseEntity.ok(new ResponseDto(RestaurantConstants.STATUS_200, RestaurantConstants.MESSAGE_200));
    }
}
