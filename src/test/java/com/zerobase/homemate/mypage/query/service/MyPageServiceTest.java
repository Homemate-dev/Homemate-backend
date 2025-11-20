package com.zerobase.homemate.mypage.query.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.entity.enums.SocialProvider;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mypage.query.dto.MyPageDto;
import com.zerobase.homemate.mypage.query.dto.MyPageResponseDto;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private BadgeService badgeService;

    @Test
    @DisplayName("마이페이지 조회 성공")
    void getMyPage() {
        // given
        Long userId = 1L;
        MyPageDto myPageDto = new MyPageDto(
                1L, SocialProvider.KAKAO, "Nickname", "https://img",
                LocalDateTime.of(2025, 9, 19, 7, 10),
                LocalDateTime.of(2025, 9, 19, 7, 10),
                LocalDateTime.of(2025, 9, 19, 7, 10),
                false, true, false,
                LocalTime.of(18, 0)
        );
        MyPageResponseDto myPageResponseDto = MyPageResponseDto.of(myPageDto, 0, 0);

        given(userRepository.findMyPageById(userId)).willReturn(Optional.of(myPageDto));
        given(badgeService.getAcquiredBadges(userId)).willReturn(List.of());

        // when
        MyPageResponseDto result = myPageService.getMyPage(userId);

        // then
        assertThat(result).isEqualTo(myPageResponseDto);
    }

    @Test
    @DisplayName("마이페이지 조회 실패 - USER_NOT_FOUND")
    void getMyPageUserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findMyPageById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myPageService.getMyPage(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}
