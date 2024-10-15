package com.urlshortener.service;

import com.urlshortener.dto.ShortUrlResponseDto;

public interface UrlShortenerService {

    ShortUrlResponseDto createShortenedUrl(String originalUrl, String customId, Long ttl);

    String getOriginalUrl(String shortCode);

    void deleteShortenedUrl(String shortCode);
}
