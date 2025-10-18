package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {


    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = User.builder()
                .id(annotation.id())
                .profileName(annotation.username())
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(user, "password", List.of());
        context.setAuthentication(auth);
        return context;
    }
}
