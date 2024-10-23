package com.urlshortener.url.service;

import com.urlshortener.url.dto.ShortUrlResponseDto;

public interface UrlShortenerService {

    ShortUrlResponseDto createShortenedUrl(String originalUrl, String customId, Long ttl);

    String getOriginalUrl(String shortCode);

    void deleteShortenedUrl(String shortCode);
}
