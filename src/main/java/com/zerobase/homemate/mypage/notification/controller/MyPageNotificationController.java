package com.zerobase.homemate.mypage.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.mypage.notification.service.MyPageNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/notification-settings")
public class MyPageNotificationController {
  private final MyPageNotificationService myPageNotificationService;

  @GetMapping("/first-setup-status")
  public ResponseEntity<FirstSetupStatusResponse> getStatus(@AuthenticationPrincipal UserPrincipal user) {
    return ResponseEntity.ok(myPageNotificationService.getFirstSetupStatus(user.id()));
  }
}
