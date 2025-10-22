package com.zerobase.homemate.badge.controller;


import com.zerobase.homemate.auth.service.AuthService;
import com.zerobase.homemate.badge.BadgeResponse;
import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.entity.User;
import lombok.RequiredArgsConstructor;
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
    public List<BadgeResponse> getMyBadges(@AuthenticationPrincipal User user){
        return badgeService.getAcquiredBadges(user);
    }

    // 취득까지 얼마 안 남은 배지 상위 3개 조회
    @GetMapping("/closest")
    public List<BadgeResponse> getClosestBadges(@AuthenticationPrincipal User user){
        return badgeService.getClosestBadges(user);
    }
}
