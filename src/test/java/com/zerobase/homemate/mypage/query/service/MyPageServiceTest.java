package com.zerobase.homemate.mypage.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private MyPageService myPageService;

  @Test
  @DisplayName("마이페이지 조회 성공")
  void getMyPage() {
    // given
    Long userId = 1L;
    MyPageResponseDto dto = new MyPageResponseDto(
        1L, SocialProvider.KAKAO, "Nickname", "https://img",
        LocalDateTime.of(2025, 9, 19, 7, 10),
        LocalDateTime.of(2025, 9, 19, 7, 10),
        false, true, false,
        LocalTime.of(18, 0),
        LocalDateTime.of(2025, 9, 19, 7, 10)
    );
    given(userRepository.findMyPageResponseById(userId))
        .willReturn(Optional.of(dto));

    // when
    MyPageResponseDto result = myPageService.getMyPage(userId);

    // then
    assertThat(result).isEqualTo(dto);
    then(userRepository).should().findMyPageResponseById(userId);
  }

  @Test
  @DisplayName("마이페이지 조회 실패 - USER_NOT_FOUND")
  void getMyPageUserNotFound() {
    // given
    Long userId = 999L;
    given(userRepository.findMyPageResponseById(userId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> myPageService.getMyPage(userId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    then(userRepository).should().findMyPageResponseById(userId);
  }
}
