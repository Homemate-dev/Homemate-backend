package com.zerobase.homemate.auth.security;

import com.zerobase.homemate.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** 컨트롤러에서 @AuthenticationPrincipal로 바로 받는 용도 */
public record UserPrincipal(Long id, String nickname, String role) {
  public static UserPrincipal from(User user) {
    return new UserPrincipal(user.getId(), user.getProfileName(), user.getUserRole().name());
  }

  public Collection<? extends GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }
}
