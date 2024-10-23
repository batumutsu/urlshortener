package com.urlshortener.common.helpers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.urlshortener.auth.model.User;

@Component
public class AuthenticationHelpers {

  /**
   * Checks if the current request is anonymous/public (not authenticated)
   */
  public boolean isAnonymous() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null ||
        authentication instanceof AnonymousAuthenticationToken ||
        !authentication.isAuthenticated();
  }

  /**
   * Gets the current authenticated user, or null if anonymous
   */
  public User getCurrentUser() {
    if (isAnonymous()) {
      return null;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (User) authentication.getPrincipal();
  }

  /**
   * Gets the current user's username, or null if anonymous
   */
  public String getCurrentUsername() {
    if (isAnonymous()) {
      return null;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

  /**
   * Convenience method for checking if there's an authenticated user
   */
  public boolean hasAuthenticatedUser() {
    return !isAnonymous();
  }
}
