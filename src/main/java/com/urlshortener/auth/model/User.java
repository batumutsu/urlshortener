package com.urlshortener.auth.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.urlshortener.common.enums.UserEnums;
import com.urlshortener.common.models.BaseEntity;
import com.urlshortener.url.models.ShortenedUrl;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_email", columnList = "email")})
public class User extends BaseEntity implements UserDetails {

  @Column(nullable = false)
  private String fullName;

  @Column(unique = true, length = 100, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "role")
  @Enumerated(EnumType.STRING)
  private List<UserEnums.Role> role;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Token> tokens;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<ShortenedUrl> shortenedUrl;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return role.getLast().getAuthorities();
    }

  @Override
    public String getUsername() {
      return email;
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
}
