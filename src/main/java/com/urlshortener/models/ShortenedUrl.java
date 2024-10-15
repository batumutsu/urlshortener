package com.urlshortener.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "short_urls", indexes = {@Index(name = "idx_short_and_original_url", columnList = "shortCode, originalUrl")})
public class ShortenedUrl extends BaseEntity {
    @Column(unique = true, nullable = false, length = 6)
    private String shortCode;

    @Column(nullable = false)
    private String originalUrl;

    private LocalDateTime expiresAt;

    private Long clicks;
}

