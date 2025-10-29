package com.zerobase.homemate.mypage.query.service;

import com.zerobase.homemate.badge.BadgeResponse;
import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {
  private final UserRepository userRepository;
  private final BadgeService badgeService;

  @Transactional(readOnly = true)
  public MyPageResponseDto getMyPage(Long userId) {
    List<BadgeResponse> badges = badgeService.getAcquiredBadges(userId);
    int acquiredBadgesCount =
        badges.stream().filter(BadgeResponse::acquired).toList().size();
    return userRepository.findMyPageResponseById(userId, badges.size(), acquiredBadgesCount)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }
}
