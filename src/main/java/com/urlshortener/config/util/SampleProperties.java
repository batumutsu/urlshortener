package com.urlshortener.config.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "generator")
@Getter
@Setter
public class SampleProperties {
    private String alphabets;
    private int length;
}
