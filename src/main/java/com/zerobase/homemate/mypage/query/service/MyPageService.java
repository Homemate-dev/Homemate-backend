package com.zerobase.homemate.mypage.query.service;

import com.zerobase.homemate.badge.BadgeProgressResponse;
import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.query.dto.MyPageDto;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserRepository userRepository;
    private final BadgeService badgeService;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(Long userId) {
        MyPageDto myPage = userRepository.findMyPageById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<BadgeProgressResponse> badges = badgeService.getAcquiredBadges(userId);
        int acquiredBadgesCount = (int) badges.stream()
                .filter(BadgeProgressResponse::acquired)
                .count();

        return MyPageResponseDto.of(myPage, badges.size(), acquiredBadgesCount);
    }
}
