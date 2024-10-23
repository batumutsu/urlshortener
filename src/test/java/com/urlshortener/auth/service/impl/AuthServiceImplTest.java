package com.urlshortener.auth.service.impl;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.auth.model.User;
import com.urlshortener.auth.model.dto.LoginUserDto;
import com.urlshortener.auth.model.dto.RegisterUserDto;
import com.urlshortener.auth.repository.TokenRepository;
import com.urlshortener.auth.repository.UserRepository;
import com.urlshortener.auth.service.AuthService;
import com.urlshortener.auth.service.AuthServiceTest;
import com.urlshortener.common.enums.UserEnums;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth Service Tests")
public class AuthServiceImplTest implements AuthServiceTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TokenRepository tokenRepository;

  private RegisterUserDto validRegisterDto;
  private LoginUserDto validLoginDto;


  @Nested
  @DisplayName("POST /auth/signup")
  class SignupTests {

    @BeforeEach
    void setUp() {
      userRepository.deleteAll();
      tokenRepository.deleteAll();
      validRegisterDto = new RegisterUserDto();
      validRegisterDto.setEmail("test@example.com");
      validRegisterDto.setPassword("Password123!");
      validRegisterDto.setFullName("Test User");
      validRegisterDto.setRole(Collections.singletonList(UserEnums.Role.USER));
    }
    @Test
    @DisplayName("Should successfully register a new user")
    void whenValidInput_thenReturns201() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validRegisterDto)))
          .andExpect(status().isCreated())
          .andExpect(MockMvcResultMatchers.jsonPath("$").value("User " + validRegisterDto.getEmail() + " was successfully registered"));

      User foundUser = userRepository.findByEmail(validRegisterDto.getEmail()).orElse(null);
      assert foundUser != null;
      assertNotNull(foundUser);
      assertEquals(1, foundUser.getRole().size());
      assertEquals(UserEnums.Role.USER, foundUser.getRole().get(0));
    }

    @Test
    @DisplayName("Should return 409 when user already exists")
    void whenUserExists_thenReturns409() throws Exception {
      userRepository.save(User.builder().email(validRegisterDto.getEmail())
          .fullName(validRegisterDto.getFullName())
          .role(validRegisterDto.getRole())
          .password(validRegisterDto.getPassword()).build());

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validRegisterDto)))
          .andExpect(status().isConflict());
    }
//
//    @Test
//    @DisplayName("Should return 400 when email is invalid")
//    void whenInvalidEmail_thenReturns400() throws Exception {
//      validRegisterDto.setEmail("invalid-email");
//
//      mockMvc.perform(post("/auth/signup")
//              .contentType(MediaType.APPLICATION_JSON)
//              .content(objectMapper.writeValueAsString(validRegisterDto)))
//          .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("Should return 400 when password is invalid")
//    void whenInvalidPassword_thenReturns400() throws Exception {
//      validRegisterDto.setPassword("");
//
//      mockMvc.perform(post("/auth/signup")
//              .contentType(MediaType.APPLICATION_JSON)
//              .content(objectMapper.writeValueAsString(validRegisterDto)))
//          .andExpect(status().isBadRequest());
//    }
  }

  @Nested
  @DisplayName("POST /auth/login")
  class LoginTests {

    @BeforeEach
    void setUp() throws Exception {
      userRepository.deleteAll();
      tokenRepository.deleteAll();
      validRegisterDto = new RegisterUserDto();
      validRegisterDto.setEmail("test@example.com");
      validRegisterDto.setPassword("Password123!");
      validRegisterDto.setFullName("Test User");
      validRegisterDto.setRole(Collections.singletonList(UserEnums.Role.USER));
      mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validRegisterDto)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should successfully authenticate user")
    void whenValidCredentials_thenReturns200() throws Exception {
      validLoginDto = LoginUserDto.builder().email(validRegisterDto.getEmail()).password(validRegisterDto.getPassword()).build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().isOk())
          .andExpect(MockMvcResultMatchers.jsonPath("$.token").isNotEmpty())
          .andExpect(MockMvcResultMatchers.jsonPath("$.expiresIn").value("3600000"));
    }

    @Test
    @DisplayName("Should return 401 when user does not exist")
    void whenUserNotFound_thenReturns401() throws Exception {
      validLoginDto = LoginUserDto.builder().email("invalid-email@gmail.com").password(validRegisterDto.getPassword()).build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().is(401));
    }

    @Test
    @DisplayName("Should return 401 when password is incorrect")
    void whenWrongPassword_thenReturns401() throws Exception {
      validLoginDto = LoginUserDto.builder().email(validRegisterDto.getEmail()).password("Wrong@Password123").build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().is(401));
    }

    @Test
    @DisplayName("Should return 400 when trying to login with an invalid email")
    void whenInvalidEmail_thenReturns400() throws Exception {
      validLoginDto = LoginUserDto.builder().email("invalid-email").password(validRegisterDto.getPassword()).build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when trying to login with a request body is empty")
    void whenEmptyRequestBody_thenReturns400() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when trying to signup with an invalid email")
    void whenInvalidEmailDuringSignup_thenReturns400() throws Exception {
      validLoginDto = LoginUserDto.builder().email("invalid-email").password(validRegisterDto.getPassword()).build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when trying to signup with an invalid password")
    void whenInvalidPasswordDuringSignup_thenReturns400() throws Exception {
      validLoginDto = LoginUserDto.builder().email("invalid-email@gmail.com").password("invalid-password").build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when trying to signup with an empty full name")
    void whenEmptyFullNameDuringSignup_thenReturns400() throws Exception {
      validLoginDto = LoginUserDto.builder().email(validRegisterDto.getEmail()).password(validRegisterDto.getPassword()).build();

      mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(validLoginDto)))
          .andExpect(status().isBadRequest());
    }
  }
}
