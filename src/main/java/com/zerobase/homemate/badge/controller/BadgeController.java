package com.zerobase.homemate.badge.controller;


import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.badge.BadgeProgressResponse;
import com.zerobase.homemate.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeService badgeService;

    // 내가 가진 배지 목록 조회
    @GetMapping("/acquired")
    public ResponseEntity<List<BadgeProgressResponse>> getMyBadges(@AuthenticationPrincipal UserPrincipal userPrincipal){

        List<BadgeProgressResponse> badges = badgeService.getAcquiredBadges(userPrincipal.id());

        return ResponseEntity.ok(badges);
    }

    // 취득까지 얼마 안 남은 배지 상위 3개 조회
    @GetMapping("/closest")
    public ResponseEntity<List<BadgeProgressResponse>> getClosestBadges(@AuthenticationPrincipal UserPrincipal userPrincipal){

        List<BadgeProgressResponse> closest = badgeService.getClosestBadges(userPrincipal.id());

        return ResponseEntity.ok(closest);
    }
}
