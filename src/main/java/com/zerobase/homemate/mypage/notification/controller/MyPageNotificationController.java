package com.zerobase.homemate.mypage.notification.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupRequest;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupResponse;
import com.zerobase.homemate.mypage.notification.dto.FirstSetupStatusDto.FirstSetupStatusResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationSettingDto.ToggleRequest;
import com.zerobase.homemate.mypage.notification.dto.NotificationSettingDto.ToggleResponse;
import com.zerobase.homemate.mypage.notification.dto.NotificationTimeDto.NotiTimeRequest;
import com.zerobase.homemate.mypage.notification.dto.NotificationTimeDto.NotiTimeResponse;
import com.zerobase.homemate.mypage.notification.service.MyPageNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @PatchMapping("time")
  public ResponseEntity<NotiTimeResponse> updateTime(
      @AuthenticationPrincipal UserPrincipal user,
      @Valid @RequestBody NotiTimeRequest request) {

    return ResponseEntity.ok(
        myPageNotificationService.updateNotificationTime(user.id(), request.notificationTime()));
  }

  @PatchMapping("/{type}")
  public ResponseEntity<ToggleResponse> toggle(
      @AuthenticationPrincipal UserPrincipal user,
      @PathVariable String type,
      @Valid @RequestBody ToggleRequest request) {

    return ResponseEntity.ok(
        myPageNotificationService.toggleNotification(user.id(), type, request.enabled()));
  }
}
