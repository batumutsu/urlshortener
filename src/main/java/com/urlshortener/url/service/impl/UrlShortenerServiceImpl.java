package com.urlshortener.url.service.impl;

import com.urlshortener.auth.model.User;
import com.urlshortener.common.helpers.AuthenticationHelpers;
import com.urlshortener.config.exception.ConflictException;
import com.urlshortener.config.exception.NotFoundException;
import com.urlshortener.config.util.GenerateUniqueShortCode;
import com.urlshortener.url.dto.ShortUrlResponseDto;
import com.urlshortener.url.models.ShortenedUrl;
import com.urlshortener.url.repository.ShortenedUrlRepository;
import com.urlshortener.url.service.UrlShortenerService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final ShortenedUrlRepository shortenedUrlRepository;
    private final GenerateUniqueShortCode generateUniqueShortCode;
    private final JobScheduler jobScheduler;
    private final AuthenticationHelpers authenticationHelpers;

    @Override
    @Transactional
    public ShortUrlResponseDto createShortenedUrl(String originalUrl, String customId, Long ttl) {
        User currentUser = authenticationHelpers.getCurrentUser();

        LocalDateTime expiresAt = ttl != null ? LocalDateTime.now().plusSeconds(ttl) : null;

        // Check if original URL already exists when custom ID is not provided
        ShortenedUrl existingShortenedUrl = customId == null ? currentUser != null
            ? shortenedUrlRepository.findByOriginalUrlAndUser(originalUrl, currentUser)
            : shortenedUrlRepository.findByOriginalUrlAndUserIsNull(originalUrl): null;

        if (existingShortenedUrl != null) {
            log.info("Original URL {} already exists", originalUrl);
            return new ShortUrlResponseDto(existingShortenedUrl);
        }

        // Generate custom ID if not provided
        String shortCode = customId != null ? customId : generateUniqueShortCode.generateUniqueShortCode(shortenedUrlRepository.findAll());

        if (customId != null && shortenedUrlRepository.findByShortCode(shortCode).isPresent()) {
            throw new ConflictException("Custom ID " + customId + " already exists");
        }

        if(expiresAt != null) {
            log.info("Schedule Delete expired url job for short code: {} at: {}", shortCode, expiresAt);
            jobScheduler.schedule(expiresAt, () -> deleteExpiredUrl(shortCode));
        }

        ShortenedUrl shortenedUrl = shortenedUrlRepository.save(new ShortenedUrl(shortCode, originalUrl, expiresAt, 0L, currentUser));

        log.info("Created shortened URL with code: {}", shortenedUrl.getShortCode());
        return new ShortUrlResponseDto(shortenedUrl);
    }

    @Override
    @Transactional
    public String getOriginalUrl(String shortCode) {
        User currentUser = authenticationHelpers.getCurrentUser();

        Optional<ShortenedUrl> optionalShortenedUrl = currentUser != null
            ? shortenedUrlRepository.findByShortCodeAndUser(shortCode, currentUser)
            : shortenedUrlRepository.findByShortCodeAndUserIsNull(shortCode);

        if (optionalShortenedUrl.isEmpty() || (optionalShortenedUrl.get().getExpiresAt() != null &&
                optionalShortenedUrl.get().getExpiresAt().isBefore(LocalDateTime.now()))) {
            throw new NotFoundException("Short URL not found or does not belong to current user");
        }
        ShortenedUrl shortenedUrl = optionalShortenedUrl.get();
        shortenedUrl.setClicks(shortenedUrl.getClicks() + 1L);
        shortenedUrlRepository.save(shortenedUrl);
        log.info("Retrieved original URL: {}", shortenedUrl.getOriginalUrl());
        return shortenedUrl.getOriginalUrl();
    }

    @Override
    @Transactional
    public void deleteShortenedUrl(String shortCode) {
        User currentUser = authenticationHelpers.getCurrentUser();

        Optional<ShortenedUrl> shortenedUrl = currentUser != null
            ? shortenedUrlRepository.findByShortCodeAndUser(shortCode, currentUser)
            : shortenedUrlRepository.findByShortCodeAndUserIsNull(shortCode);

        if(shortenedUrl.isPresent()){
            log.info("Deleted URL with code: {}", shortCode);
            shortenedUrlRepository.deleteByShortCode(shortCode);
        }
        else
        {
            throw new NotFoundException("Short URL not found or does not belong to current user");
        }
    }

    @Transactional
    @Job(name = "Delete expired url")
    public void deleteExpiredUrl(String shortCode) {
        shortenedUrlRepository.deleteByShortCode(shortCode);
        log.info("Job - deleted URL with code: {}", shortCode);
    }

    @Transactional
    @Job(name = "Delete all expired urls", labels = "url-shortener-delete-all-expired-urls", retries = -1)
    public void deleteExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = shortenedUrlRepository.deleteAllByExpiresAtBefore(now);
        log.info("Job - deleted {} expired short URLs before: {}", deletedCount, now);

    }

    @PostConstruct
    public void scheduleRecurrently() {
        log.info("Registering Delete all expired urls job for every half hour");
        jobScheduler.scheduleRecurrently(Cron.everyHalfHour(), this::deleteExpiredUrls);
    }
}
