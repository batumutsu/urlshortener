package com.urlshortener.auth.service;

import com.urlshortener.auth.model.dto.LoginResponse;
import com.urlshortener.auth.model.dto.LoginUserDto;
import com.urlshortener.auth.model.dto.RegisterUserDto;

public interface AuthService {
  String signup(RegisterUserDto input);

  LoginResponse authenticate(LoginUserDto input);
}
