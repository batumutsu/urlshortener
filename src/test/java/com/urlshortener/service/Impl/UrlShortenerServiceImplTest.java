package com.urlshortener.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortenUrlRequestDto;
import com.urlshortener.models.ShortenedUrl;
import com.urlshortener.repository.ShortenedUrlRepository;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortenedUrlRepository shortUrlRepository;

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        shortUrlRepository.deleteAll();
    }

    @Test
    void createShortUrl_Success() throws Exception {
        ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl("https://example.com").build();

        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
            .andExpect(jsonPath("$.shortCode").isString())
            .andExpect(jsonPath("$.clicks").value(0));

        assertEquals(1, shortUrlRepository.count());
    }

    @Test
    void createShortUrl_DuplicateId() throws Exception {
        String customId = "abc123";
        shortUrlRepository.save(new ShortenedUrl(customId, "https://example.com", null, 0L));

        ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl("https://example.com").customId(customId).build();

        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    void getOriginalUrl_Success() throws Exception {
        String shortCode = "abc123";
        String originalUrl = "https://example.com";
        shortUrlRepository.save(new ShortenedUrl(shortCode, originalUrl, null, 0L));

        mockMvc.perform(get("/{shortCode}", shortCode))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", originalUrl));
    }

    @Test
    void getOriginalUrl_NotFound() throws Exception {
        mockMvc.perform(get("/nonexistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getOriginalUrl_Expired() throws Exception {
        String shortCode = "expire";
        String originalUrl = "https://example.com";
        ShortenedUrl expiredUrl = new ShortenedUrl(shortCode, originalUrl, LocalDateTime.now().minusSeconds(1), 0L);
        shortUrlRepository.save(expiredUrl);

        mockMvc.perform(get("/{shortCode}", shortCode))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteShortUrl_Success() throws Exception {
        String shortCode = "delete";
        shortUrlRepository.save(new ShortenedUrl(shortCode, "https://example.com", null, 0L));

        mockMvc.perform(delete("/{shortCode}", shortCode))
            .andExpect(status().isOk());

        assertFalse(shortUrlRepository.findByShortCode(shortCode).isPresent());
    }
}