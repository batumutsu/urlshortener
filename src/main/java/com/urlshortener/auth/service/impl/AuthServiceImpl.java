package com.urlshortener.auth.service.impl;

import java.util.Collections;

import org.apache.tomcat.util.json.JSONParser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.urlshortener.auth.model.dto.LoginResponse;
import com.urlshortener.auth.model.dto.RegisterUserDto;
import com.urlshortener.auth.repository.TokenRepository;
import com.urlshortener.auth.service.AuthService;
import com.urlshortener.auth.model.User;
import com.urlshortener.auth.repository.UserRepository;
import com.urlshortener.auth.model.dto.LoginUserDto;
import com.urlshortener.common.enums.TokenEnums;
import com.urlshortener.common.enums.UserEnums;
import com.urlshortener.config.exception.ConflictException;
import com.urlshortener.config.exception.NotFoundException;
import com.urlshortener.config.jwt.JwtUtils;
import com.urlshortener.auth.model.Token;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final AuthenticationManager authenticationManager;

  private final JwtUtils jwtUtils;

  private final TokenRepository tokenRepository;

  @Override
  @Transactional
  public String signup(RegisterUserDto input) {
    if (userRepository.findByEmail(input.getEmail()).isPresent()) {
      throw new ConflictException("Email already exists");
    }

    User user = User.builder().fullName(input.getFullName())
        .email(input.getEmail())
        .role(input.getRole() == null ? Collections.singletonList(UserEnums.Role.USER) : input.getRole())
        .password(passwordEncoder.encode(input.getPassword())).build();
log.info("User {} is about to be successfully registered", user.getFullName());
    log.info("User {} is about to be successfully registered", user.getEmail());
    log.info("User {} is about to be successfully registered", user.getRole());
    log.info("User {} is about to be successfully registered", user.getPassword());
    userRepository.save(user);
    return "User " + user.getEmail() + " was successfully registered";
  }

  @Override
  @Transactional
  public LoginResponse authenticate(LoginUserDto loginUserDto) {
     authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginUserDto.getEmail(),
            loginUserDto.getPassword()
        )
    );

        User authenticatedUser = userRepository.findByEmail(loginUserDto.getEmail())
        .orElseThrow(() -> new NotFoundException("User not found"));

      revokeAllUserTokens(authenticatedUser);
      String jwtToken = jwtUtils.generateToken(authenticatedUser);
      saveUserToken(authenticatedUser, jwtToken);
      return LoginResponse.builder().token(jwtToken).expiresIn(jwtUtils.getExpirationTime()).build();
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  private void saveUserToken(User user, String generatedToken) {
    var token = Token.builder()
        .user(user)
        .token(generatedToken)
        .type(TokenEnums.Type.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }
}
