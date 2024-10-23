package com.urlshortener.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.auth.model.dto.LoginResponse;
import com.urlshortener.auth.model.dto.LoginUserDto;
import com.urlshortener.auth.model.dto.RegisterUserDto;
import com.urlshortener.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth URL Shortener API", description = "Operations for authenticating and managing users")
public class AuthController {

  @Autowired
  private AuthService authService;

  @PostMapping("/signup")
  @Operation(summary = "Create a new user", description = "Creates a new user with the provided email, password and full name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "409", description = "User already exists", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid Email", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid Password", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid Full Name", content = @Content),
      @ApiResponse(responseCode = "400", description = "Blank body", content = @Content)
  })
  public ResponseEntity<String> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(registerUserDto));
  }

  @PostMapping("/login")
  @Operation(summary = "Get an authentication token", description = "Get an authentication token with the provided email and password")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", content = { @Content(schema = @Schema(implementation = LoginResponse.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "401", description = "Bad credentials", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid Email", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid Password", content = @Content),
      @ApiResponse(responseCode = "400", description = "Blank body", content = @Content)
  })
  public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
    LoginResponse loginResponse = authService.authenticate(loginUserDto);
    return ResponseEntity.ok(loginResponse);
  }
}
