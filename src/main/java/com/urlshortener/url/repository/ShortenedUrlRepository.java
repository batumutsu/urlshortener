package com.urlshortener.url.repository;

import com.urlshortener.auth.model.User;
import com.urlshortener.url.models.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, String> {
    Optional<ShortenedUrl> findByShortCodeAndUserIsNull(String shortCode);
    ShortenedUrl findByOriginalUrlAndUserIsNull(String originalUrl);
    void deleteByShortCode(String shortCode);
    int deleteAllByExpiresAtBefore(LocalDateTime now);

    ShortenedUrl findByOriginalUrlAndUser(String originalUrl, User user);

    Optional<ShortenedUrl>  findByShortCodeAndUser(String shortCode, User user);

    Optional<ShortenedUrl> findByShortCode(String shortCode);
}
