package com.zerobase.homemate.mypage.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupRequest;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupResponse;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationSettingDto.MasterToggleRequest;
import com.zerobase.homemate.mypage.notification.dto.NotificationSettingDto.MasterToggleResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationTimeDto.NotiTimeResponse;
import com.zerobase.homemate.mypage.notification.service.MyPageNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  
  @PostMapping("/first-setup")
  public ResponseEntity<FirstSetupResponse> firstSetup(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody FirstSetupRequest request) {

    return ResponseEntity.ok(
        myPageNotificationService.completeFirstSetup(user.id(), request.notificationTime()));
  }
  
  @GetMapping("/time")
  public ResponseEntity<NotiTimeResponse> getTime(@AuthenticationPrincipal UserPrincipal user) {
    return ResponseEntity.ok(myPageNotificationService.getNotificationTime(user.id()));
  }

  @PatchMapping("/master")
  public ResponseEntity<MasterToggleResponse> toggleMaster(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody MasterToggleRequest request) {

    return ResponseEntity.ok(myPageNotificationService.toggleMaster(user.id(), request.enabled()));
  }
}
