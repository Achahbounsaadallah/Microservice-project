package com.example.restaurant.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ErrorResponseDto {
    private String apiPath;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime errorTime;
}
