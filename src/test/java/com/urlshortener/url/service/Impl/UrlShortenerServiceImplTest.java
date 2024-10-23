package com.urlshortener.url.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.auth.model.User;
import com.urlshortener.auth.model.dto.LoginResponse;
import com.urlshortener.auth.model.dto.LoginUserDto;
import com.urlshortener.auth.model.dto.RegisterUserDto;
import com.urlshortener.auth.repository.UserRepository;
import com.urlshortener.common.enums.UserEnums;
import com.urlshortener.url.dto.ShortenUrlRequestDto;
import com.urlshortener.url.models.ShortenedUrl;
import com.urlshortener.url.repository.ShortenedUrlRepository;
import com.urlshortener.url.service.UrlShortenerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import lombok.Builder;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Create Short Url Tests")
class UrlShortenerServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortenedUrlRepository shortUrlRepository;

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private LoginResponse validLoginResponse = new LoginResponse();
    private User authenticatedUser = new User();

    @BeforeAll
    void setUp() throws Exception {
        userRepository.deleteAll();
        RegisterUserDto validRegisterDto = new RegisterUserDto();
        validRegisterDto.setEmail("test1@example.com");
        validRegisterDto.setPassword("Password123!");
        validRegisterDto.setFullName("Test User");
        validRegisterDto.setRole(Collections.singletonList(UserEnums.Role.USER));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterDto)))
            .andExpect(status().isCreated());

        LoginUserDto validLoginDto = LoginUserDto.builder().email(validRegisterDto.getEmail()).password(
            validRegisterDto.getPassword()).build();

        Optional<User> user = userRepository.findByEmail(validRegisterDto.getEmail());
        authenticatedUser = user.orElse(null);

        MvcResult requestResult = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginDto)))
            .andExpect(status().isOk())
            .andReturn();

        validLoginResponse = objectMapper.readValue(requestResult.getResponse().getContentAsString(), LoginResponse.class);

    }
    @Nested
    @DisplayName("Create Short Url without a token.")
    class UnauthenticatedCreateShortUrlTests {
        @BeforeEach
        void setUp() {
            shortUrlRepository.deleteAll();
        }
        @Test
        @DisplayName("Should successfully create short url without a token.")
        void createShortUrl_Success() throws Exception {
            ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl(
                "https://www.example.com").build();

            mockMvc.perform(post("/mixed/url/shorten")
                    .header("Authorization", "Bearer " + validLoginResponse.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());

            testRequest.setCustomId("abc123");

            mockMvc.perform(post("/mixed/url/shorten")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.shortCode").isString())
                .andExpect(jsonPath("$.clicks").value(0));

            assertNotNull(shortUrlRepository.findByShortCodeAndUserIsNull(testRequest.getCustomId()));
        }

        @Test
        @DisplayName("Should return 409 when short code already exists without a token.")
        void createShortUrl_DuplicateId() throws Exception {
            String customId = "abc123";
            shortUrlRepository.save(new ShortenedUrl(customId, "https://www.example.com", null, 0L, null));

            ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl(
                "https://example.com").customId(customId).build();

            mockMvc.perform(post("/mixed/url/shorten")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 200 when short code already exists without a token.")
        void getOriginalUrl_Success() throws Exception {
            String shortCode = "abc123";
            String originalUrl = "https://www.example.com";
            shortUrlRepository.save(new ShortenedUrl(shortCode, originalUrl, null, 0L, null));

            mockMvc.perform(get("/mixed/url/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));
        }

        @Test
        @DisplayName("Should return 404 when short code does not exist or belongs to a user, without a token.")
        void getOriginalUrl_NotFound() throws Exception {
            ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl(
                "https://www.example.com").customId("abc123").build();

            mockMvc.perform(post("/mixed/url/shorten")
                    .header("Authorization", "Bearer " + validLoginResponse.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/mixed/url/{shortCode}", testRequest.getCustomId()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when short code does not exist/expired or belongs to a user, without a token.")
        void getOriginalUrl_Expired() throws Exception {
            String shortCode = "expire";
            String originalUrl = "https://www.example.com";
            ShortenedUrl expiredUrl = new ShortenedUrl(shortCode, originalUrl, LocalDateTime.now().minusSeconds(1), 0L,
                null);
            shortUrlRepository.save(expiredUrl);

            mockMvc.perform(get("/mixed/url/{shortCode}", shortCode))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should successfully delete short url when it does not belong to a user, without a token.")
        void deleteShortUrl_Success() throws Exception {
            shortUrlRepository.save(new ShortenedUrl("cannot", "https://www.example.com", null, 0L, authenticatedUser));

            mockMvc.perform(delete("/mixed/url/{shortCode}", "cannot"))
                .andExpect(status().isNotFound());

            shortUrlRepository.save(new ShortenedUrl("delete", "https://www.example.com", null, 0L, null));

            mockMvc.perform(delete("/mixed/url/{shortCode}", "delete"))
                .andExpect(status().isOk());

            assertFalse(shortUrlRepository.findByShortCode("delete").isPresent());
        }
    }

    @Nested
    @DisplayName("Create Short Url with a token.")
    class AuthenticatedCreateShortUrlTests {

        @BeforeEach
        void setUpEach() {
            shortUrlRepository.deleteAll();
        }

        @Test
        @DisplayName("Should successfully create short url with a token.")
        void createShortUrl_Success() throws Exception {
            ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl(
                "https://www.example.com").customId("abc123").build();

            mockMvc.perform(post("/mixed/url/shorten")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());

            testRequest.setCustomId("abc124");

            mockMvc.perform(post("/mixed/url/shorten")
                    .header("Authorization", "Bearer " + validLoginResponse.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.shortCode").isString())
                .andExpect(jsonPath("$.clicks").value(0));

            assertNotNull(shortUrlRepository.findByShortCodeAndUser(testRequest.getCustomId(), authenticatedUser));

            Optional<ShortenedUrl> shortUrl = shortUrlRepository.findByShortCode(testRequest.getCustomId());

            assertTrue(shortUrl.isPresent());
            assertNotNull(shortUrl.get().getUser());
        }

        @Test
        @DisplayName("Should return 409 when short code already exists, with a token.")
        void createShortUrl_DuplicateId() throws Exception {
            String customId = "abc123";

            shortUrlRepository.save(new ShortenedUrl(customId, "https://www.example.com", null, 0L, authenticatedUser));

            ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl(
                "https://www.example.com").customId(customId).build();

            mockMvc.perform(post("/mixed/url/shorten")
                    .header("Authorization", "Bearer " + validLoginResponse.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isConflict());

            Optional<ShortenedUrl> shortUrl = shortUrlRepository.findByShortCode(testRequest.getCustomId());

            assertTrue(shortUrl.isPresent());
            assertNotNull(shortUrl.get().getUser());
        }

        @Test
        @DisplayName("Should return 302 when short code exists and belongs to a user, with a token.")
        void getOriginalUrl_Success() throws Exception {
            String shortCode = "abc123";
            String originalUrl = "https://www.example.com";

            shortUrlRepository.save(new ShortenedUrl(shortCode, originalUrl, null, 0L, authenticatedUser));

            mockMvc.perform(get("/mixed/url/{shortCode}", shortCode)
                    .header("Authorization", "Bearer " + validLoginResponse.getToken()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));
        }

        @Test
        @DisplayName("Should return 404 when short code does not exist or belongs to a different user, with a token.")
        void getOriginalUrl_NotFound() throws Exception {

            ShortenUrlRequestDto testRequest = ShortenUrlRequestDto.builder().originalUrl(
                "https://www.example.com").customId("abc123").build();

            mockMvc.perform(post("/mixed/url/shorten")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/mixed/url/{shortCode}", testRequest.getCustomId())
                    .header("Authorization", "Bearer " + validLoginResponse.getToken()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when short code is expired, with a token.")
        void getOriginalUrl_Expired() throws Exception {
            String shortCode = "expire";
            String originalUrl = "https://www.example.com";
            ShortenedUrl expiredUrl = new ShortenedUrl(shortCode, originalUrl, LocalDateTime.now().minusSeconds(1), 0L,
                authenticatedUser);
            shortUrlRepository.save(expiredUrl);

            mockMvc.perform(get("/mixed/url/{shortCode}", shortCode)
                    .header("Authorization", "Bearer " + validLoginResponse.getToken()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should successfully delete short url when it belongs to authenticated user, with a token.")
        void deleteShortUrl_Success() throws Exception {
            String shortCode = "delete";
            shortUrlRepository.save(new ShortenedUrl(shortCode, "https://www.example.com", null, 0L, authenticatedUser));

            mockMvc.perform(delete("/mixed/url/{shortCode}", shortCode)
                    .header("Authorization", "Bearer " + validLoginResponse.getToken()))
                .andExpect(status().isOk());

            assertFalse(shortUrlRepository.findByShortCode(shortCode).isPresent());
        }

    }
}