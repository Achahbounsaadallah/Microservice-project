package com.example.restaurant.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(name = "Meal", description = "Meal information")
public class MealDto {
    @Schema(description = "Meal identifier", example = "1")
    private Long id;
    @NotEmpty(message = "Meal name is required")
    private String name;
    private String description;
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or greater")
    private BigDecimal price;
    private boolean available;
    @Positive(message = "Restaurant ID must be positive")
    private Long restaurantId;
}
