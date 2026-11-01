package com.example.restaurant.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.restaurant.constants.RestaurantConstants;
import com.example.restaurant.dto.ErrorResponseDto;
import com.example.restaurant.dto.ResponseDto;
import com.example.restaurant.dto.RestaurantDto;
import com.example.restaurant.service.IRestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Restaurants", description = "Restaurant management APIs")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class RestaurantController {
    private final IRestaurantService restaurantService;
    public RestaurantController(IRestaurantService restaurantService) { this.restaurantService = restaurantService; }

    @Operation(summary = "Create a restaurant")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Created"), @ApiResponse(responseCode = "400", description = "Validation error"), @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponseDto.class)))})
    @PostMapping(path = "/restaurants", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody RestaurantDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(dto));
    }

    @Operation(summary = "List restaurants")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Restaurants returned"), @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponseDto.class)))})
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDto>> fetchRestaurants() { return ResponseEntity.ok(restaurantService.fetchRestaurants()); }

    @Operation(summary = "Get restaurant by ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Restaurant returned"), @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponseDto.class)))})
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantDto> fetchRestaurant(@PathVariable Long id) { return ResponseEntity.ok(restaurantService.fetchRestaurant(id)); }

    @Operation(summary = "Update a restaurant")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated"), @ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "417", description = "Update failed")})
    @PutMapping(path = "/restaurants/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantDto dto) {
        boolean updated = restaurantService.updateRestaurant(id, dto);
        return updated ? ResponseEntity.ok(new ResponseDto(RestaurantConstants.STATUS_200, RestaurantConstants.MESSAGE_200))
                : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseDto(RestaurantConstants.STATUS_417, RestaurantConstants.MESSAGE_417_UPDATE));
    }

    @Operation(summary = "Delete a restaurant")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted"), @ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "417", description = "Delete failed")})
    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<ResponseDto> deleteRestaurant(@PathVariable Long id) {
        boolean deleted = restaurantService.deleteRestaurant(id);
        return deleted ? ResponseEntity.ok(new ResponseDto(RestaurantConstants.STATUS_200, RestaurantConstants.MESSAGE_200))
                : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseDto(RestaurantConstants.STATUS_417, RestaurantConstants.MESSAGE_417_DELETE));
    }
}
