package com.example.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(name = "Restaurant", description = "Restaurant information")
public class RestaurantDto {
    @Schema(description = "Restaurant identifier", example = "1")
    private Long id;
    @NotEmpty(message = "Restaurant name is required")
    @Schema(description = "Restaurant name", example = "Le Bon Repas")
    private String name;
    @NotEmpty(message = "Address is required")
    private String address;
    @NotEmpty(message = "Phone is required")
    @Pattern(regexp = "^[+0-9() .-]{7,20}$", message = "Phone number is invalid")
    private String phone;
    @Schema(description = "Whether the restaurant is open", example = "true")
    private boolean open;
}
