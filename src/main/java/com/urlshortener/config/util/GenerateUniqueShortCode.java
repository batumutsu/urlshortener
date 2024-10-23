package com.urlshortener.config.util;

import com.urlshortener.url.models.ShortenedUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public final class GenerateUniqueShortCode {
    @Value("${shortener.alphabet}")
    private String alphabets;

    @Value("${shortener.id.length}")
    private int idLength;

    public String generateUniqueShortCode(List<ShortenedUrl> shortenedUrls) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(idLength);
        Set<String> shortCodes = shortenedUrls.stream()
                .map(ShortenedUrl::getShortCode)
                .collect(Collectors.toSet());

        do {
            for (int i = 0; i < idLength; i++) {
                sb.append(alphabets.charAt(random.nextInt(alphabets.length())));
            }
        } while (shortCodes.contains(sb.toString()));
        return sb.toString();
    }
}
