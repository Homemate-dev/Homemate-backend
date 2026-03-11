package com.zerobase.homemate.notification.push.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.notification.push.dto.FcmTokenDto;
import com.zerobase.homemate.notification.push.service.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/push")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/subscriptions")
    public ResponseEntity<FcmTokenDto.Response> subscribe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid FcmTokenDto.Request request
    ) {
        Long userId = userPrincipal.id();

        FcmTokenDto.Response result = fcmTokenService.registerToken(userId, request);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @RequestBody FcmTokenDto.Request request
    ) {
        fcmTokenService.deactivateToken(request);

        return ResponseEntity.noContent().build();
    }
}
