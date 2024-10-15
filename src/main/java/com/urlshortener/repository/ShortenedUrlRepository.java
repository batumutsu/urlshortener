package com.urlshortener.repository;

import com.urlshortener.models.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, String> {
    Optional<ShortenedUrl> findByShortCode(String shortCode);
    ShortenedUrl findByOriginalUrl(String originalUrl);
    void deleteByShortCode(String shortCode);
    int deleteAllByExpiresAtBefore(LocalDateTime now);
}
