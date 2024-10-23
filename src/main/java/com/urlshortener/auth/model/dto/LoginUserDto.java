package com.urlshortener.auth.model.dto;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUserDto {
  @Schema(
      description = "Email address of the user to be registered",
      example = "example@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Email is required")
  @Email(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "Please provide a valid email address"
  )
  @Length(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
  private String email;

  @Schema(
      description = "Password for the user account",
      example = "StrongP@ss123",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Password is required")
  @Length(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()]).{8,}$",
      message = "Password must contain at least one digit, one lowercase, one uppercase, " +
          "and one special character (@#$%^&+=!*())"
  )
  private String password;
}
