package com.zerobase.homemate.mypage.query.service;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public MyPageResponseDto getMyPage(Long userId) {
    return userRepository.findMyPageResponseById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }
}
