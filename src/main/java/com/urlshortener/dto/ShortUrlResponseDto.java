package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.urlshortener.models.ShortenedUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Shortened URL response body")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShortUrlResponseDto implements Serializable {
    @Schema(description = "Short code also known as short URL or id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shortCode;

    @Schema(description = "Original URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalUrl;

    @Schema(description = "Expires at", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    @Schema(description = "Clicks", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "0")
    private Long clicks;

    public ShortUrlResponseDto(@NonNull ShortenedUrl shortenedUrl) {
        this.shortCode = shortenedUrl.getShortCode();
        this.originalUrl = shortenedUrl.getOriginalUrl();
        this.expiresAt = shortenedUrl.getExpiresAt();
        this.clicks = shortenedUrl.getClicks();
    }
}
