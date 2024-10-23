package com.urlshortener.common.enums;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class UserEnums {

  @Getter
  @RequiredArgsConstructor
  public enum Role {

    USER(Collections.emptySet()),
    ADMIN(
        Set.of(
            Permission.ADMIN_READ,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_DELETE,
            Permission.ADMIN_CREATE,
            Permission.MANAGER_READ,
            Permission.MANAGER_UPDATE,
            Permission.MANAGER_DELETE,
            Permission.MANAGER_CREATE
        )
    ),
    MANAGER(
        Set.of(
            Permission.MANAGER_READ,
            Permission.MANAGER_UPDATE,
            Permission.MANAGER_DELETE,
            Permission.MANAGER_CREATE
        )
    )

    ;

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
      var authorities = getPermissions()
          .stream()
          .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
          .collect(Collectors.toList());
      authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
      return authorities;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    MANAGER_READ("management:read"),
    MANAGER_UPDATE("management:update"),
    MANAGER_CREATE("management:create"),
    MANAGER_DELETE("management:delete")

    ;

    private final String permission;
  }

}
