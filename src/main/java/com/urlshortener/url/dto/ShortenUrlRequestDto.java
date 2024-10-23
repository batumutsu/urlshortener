package com.urlshortener.url.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

@Data
@Builder
@Schema(description = "Shorten URL request body")
public class ShortenUrlRequestDto implements Serializable {
    @NotEmpty(message = "Original URL is required")
    @URL(message = "Invalid URL")
    @Schema(description = "Original URL", example = "https://www.example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalUrl;

    @Schema(description = "Custom ID to be used as a short code with max length is 6", example = "WBz0Ff", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Length(min = 1, max = 6, message = "Custom ID must be less than or equal to 6 characters")
    private String customId;

    @Schema(description = "Time-to-live in seconds", example = "200", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "TTL must be greater than zero")
    private Long ttl;
}

