package com.urlshortener.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortUrlResponseDto;
import com.urlshortener.dto.ShortenUrlRequestDto;
import com.urlshortener.models.ShortenedUrl;
import com.urlshortener.repository.ShortenedUrlRepository;
import com.urlshortener.service.UrlShortenerServiceTest;
import com.urlshortener.service.impl.UrlShortenerServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UrlShortenerServiceImplTest implements UrlShortenerServiceTest {

    @Autowired
    private ShortenedUrlRepository shortUrlRepository;

    @Autowired
    private UrlShortenerServiceImpl urlShortenerServiceImpl;

    @Mock
    private RestTemplate mockRestTemplate;

    private RestTemplate restTemplate;

    @Value("${app.url}")
    private String baseUrl;

    private static AutoCloseable mockitoCloseable;

    @BeforeAll
    static void setUpAll() {
        mockitoCloseable = MockitoAnnotations.openMocks(UrlShortenerServiceImplTest.class);
    }
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper().findAndRegisterModules());
        restTemplate.getMessageConverters().add(0, converter);
    }

    @AfterEach
    void tearDown() {
        reset(mockRestTemplate);
        shortUrlRepository.deleteAll();
    }

    @AfterAll
    static void tearDownAll(@Autowired ShortenedUrlRepository repo,
                            @Autowired JobScheduler scheduler) throws Exception {
        // Clean up autowired components
        repo.deleteAll();

        // Shutdown the job scheduler
        scheduler.shutdown();

        // Close Mockito resources
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }


    @Test
    void createShortUrl_Success() {
        ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl("https://example.com").build();

        ResponseEntity<ShortUrlResponseDto> responseEntity = sendPostRequest(testRequest, ShortUrlResponseDto.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        ShortUrlResponseDto response = responseEntity.getBody();
        ShortenedUrl shortUrl = shortUrlRepository.findAll().get(0);

        assertNotNull(response);
        assertEquals(shortUrl.getOriginalUrl(), response.getOriginalUrl());
        assertEquals(shortUrl.getShortCode(), response.getShortCode());
        assertEquals(shortUrl.getClicks(), response.getClicks());
    }

    @Test
    void createShortUrl_DuplicateId() {
        ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl("https://example.com").customId("abc123").build();
        shortUrlRepository.save(new ShortenedUrl("abc123", "https://example.com", null, 0L));

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                sendPostRequest(testRequest, String.class)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void getOriginalUrl_Success() {
        String shortCode = "abc123";
        String originalUrl = "https://example.com";
        shortUrlRepository.save(new ShortenedUrl(shortCode, originalUrl, null, 0L));

        ResponseEntity<String> responseEntity = sendGetRequest("/" + shortCode, String.class);

        assertEquals(HttpStatus.FOUND, responseEntity.getStatusCode());
        assertEquals(originalUrl, responseEntity.getHeaders().getLocation().toString());
    }

    @Test
    void getOriginalUrl_NotFound() {
        String shortCode = "nonexistent";

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                sendGetRequest("/" + shortCode, String.class)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getOriginalUrl_Expired() {
        String shortCode = "expire";
        String originalUrl = "https://example.com";
        ShortenedUrl expiredUrl = new ShortenedUrl(shortCode, originalUrl, LocalDateTime.now().minusSeconds(1), 0L);
        shortUrlRepository.save(expiredUrl);

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                sendGetRequest("/" + shortCode, String.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteShortUrl_Success() {
        String shortCode = "Delete";
        shortUrlRepository.save(new ShortenedUrl(shortCode, "https://example.com", null, 0L));

        ResponseEntity<Void> responseEntity = sendDeleteRequest("/" + shortCode, Void.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertFalse(shortUrlRepository.findByShortCode(shortCode).isPresent());
    }

    private <T> ResponseEntity<T> sendPostRequest(Object body, Class<T> responseType) {
        return restTemplate.exchange(
                baseUrl + "/shorten",
                HttpMethod.POST,
                new HttpEntity<>(body),
                responseType
        );
    }

    private <T> @NotNull ResponseEntity<T> sendGetRequest(String endpoint, Class<T> responseType) {
        return restTemplate.exchange(
                baseUrl + endpoint,
                HttpMethod.GET,
                null,
                responseType
        );
    }

    private <T> @NotNull ResponseEntity<T> sendDeleteRequest(String endpoint, Class<T> responseType) {
        return restTemplate.exchange(
                baseUrl + endpoint,
                HttpMethod.DELETE,
                null,
                responseType
        );
    }
}