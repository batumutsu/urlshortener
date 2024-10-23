package com.urlshortener.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.urlshortener.config.jwt.JwtAuthEntryPoint;
import com.urlshortener.config.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
  private final JwtAuthEntryPoint unauthorizedHandler;
  public final JwtAuthenticationFilter jwtAuthenticationFilter;


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests((requests) -> requests.requestMatchers(
            "/auth/**",
            "public/**",
            "mixed/**",
            "/static/**",
            "/api-docs/**",
            "/swagger-ui/**"
        )
        .permitAll()
        .anyRequest()
        .authenticated());

    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
    //http.formLogin(withDefaults());
    //http.httpBasic(withDefaults());
    //http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));//This used for H2 console, should be removed in production
    http.csrf(AbstractHttpConfigurer::disable);//This should be enabled in production
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.logout(logout -> logout.logoutUrl("/api/v1/auth/logout").addLogoutHandler(new SecurityContextLogoutHandler()).logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()));


    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:8000"));
    configuration.setAllowedMethods(List.of("GET","POST","DELETE"));
    configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**",configuration);

    return source;
  }
}
