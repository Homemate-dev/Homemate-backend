package com.zerobase.homemate.mypage.query.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.mypage.query.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class MyPageController {
  private final MyPageService myPageService;

  @GetMapping("/me")
  public ResponseEntity<MyPageResponseDto> getMe(@AuthenticationPrincipal UserPrincipal user) {
    return ResponseEntity.ok(myPageService.getMyPage(user.id()));
  }
}
