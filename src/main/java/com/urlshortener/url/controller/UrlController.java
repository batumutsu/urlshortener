package com.urlshortener.url.controller;

import com.urlshortener.url.dto.ShortUrlResponseDto;
import com.urlshortener.url.dto.ShortenUrlRequestDto;
import com.urlshortener.url.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Tag(name = "URL Shortener API", description = "Operations for shortening and managing URLs")
@RequestMapping("/mixed/url")
public class UrlController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    @Operation(summary = "Create a shortened URL", description = "Creates a new shortened URL from the provided original URL, ttl, customId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", content = { @Content(schema = @Schema(implementation = ShortUrlResponseDto.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "Custom ID already exists", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid URL", content = @Content),
            @ApiResponse(responseCode = "400", description = "Blank body", content = @Content)
    })
    public ResponseEntity<ShortUrlResponseDto> createShortUrl(@Valid @RequestBody ShortenUrlRequestDto shortenUrlRequest) {
        ShortUrlResponseDto shortenedUrl = urlShortenerService.createShortenedUrl(shortenUrlRequest.getOriginalUrl(), shortenUrlRequest.getCustomId(), shortenUrlRequest.getTtl());
        return ResponseEntity.status(HttpStatus.CREATED).body(shortenedUrl);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Redirect to original URL", description = "Redirects to the original URL associated with the given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Redirect to original URL", content = @Content),
            @ApiResponse(responseCode = "404", description = "Short URL not found or expired", content = @Content)
    })
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable("id") String shortCode) {
        String originalUrl = urlShortenerService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shortened URL", description = "Deletes the shortened URL associated with the given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content)
    })
    public ResponseEntity<Void> deleteShortUrl(@PathVariable("id") String shortCode) {
        urlShortenerService.deleteShortenedUrl(shortCode);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

//    @PostMapping("/public/shorten")
//    @Operation(summary = "Create a shortened URL without authentication", description = "Creates a new shortened URL from the provided original URL, ttl, customId without authentication")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "201", content = { @Content(schema = @Schema(implementation = ShortUrlResponseDto.class), mediaType = "application/json") }),
//        @ApiResponse(responseCode = "409", description = "Custom ID already exists", content = @Content),
//        @ApiResponse(responseCode = "400", description = "Invalid URL", content = @Content),
//        @ApiResponse(responseCode = "400", description = "Blank body", content = @Content)
//    })
//    public ResponseEntity<ShortUrlResponseDto> publicCreateShortUrl(@Valid @RequestBody ShortenUrlRequestDto shortenUrlRequest) {
//        ShortUrlResponseDto shortenedUrl = urlShortenerService.createShortenedUrl(shortenUrlRequest.getOriginalUrl(), shortenUrlRequest.getCustomId(), shortenUrlRequest.getTtl());
//        return ResponseEntity.status(HttpStatus.CREATED).body(shortenedUrl);
//    }
//
//    @GetMapping("/public/{id}")
//    @Operation(summary = "Redirect to original URL without authentication", description = "Redirects to the original URL associated with the given id without authentication")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Redirect to original URL", content = @Content),
//        @ApiResponse(responseCode = "404", description = "Short URL not found or expired", content = @Content)
//    })
//    public ResponseEntity<Void> publicRedirectToOriginalUrl(@PathVariable("id") String shortCode) {
//        String originalUrl = urlShortenerService.getOriginalUrl(shortCode);
//        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
//    }
//
//    @DeleteMapping("/public/{id}")
//    @Operation(summary = "Delete a shortened URL without authentication", description = "Deletes the shortened URL associated with the given id without authentication")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", content = @Content)
//    })
//    public ResponseEntity<Void> publicDeleteShortUrl(@PathVariable("id") String shortCode) {
//        urlShortenerService.deleteShortenedUrl(shortCode);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
}