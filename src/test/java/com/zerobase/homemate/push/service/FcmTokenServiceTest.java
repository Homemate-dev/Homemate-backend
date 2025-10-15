package com.zerobase.homemate.push.service;

import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.DeviceType;
import com.zerobase.homemate.push.dto.FcmTokenDto;
import com.zerobase.homemate.repository.FcmTokenRepository;
import com.zerobase.homemate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private FcmTokenService fcmTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
    }

    @Test
    void registerToken_Success_WithNewToken() {
        // given
        Long userId = 1L;
        String token = "test-token";
        DeviceType deviceType = DeviceType.WEB;
        FcmTokenDto.Request request = new FcmTokenDto.Request();
        ReflectionTestUtils.setField(request, "token", token);
        ReflectionTestUtils.setField(request, "deviceType", deviceType);

        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        ArgumentCaptor<FcmToken> captor = ArgumentCaptor.forClass(FcmToken.class);
        when(fcmTokenRepository.save(any(FcmToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FcmTokenDto.Response response = fcmTokenService.registerToken(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getDeviceType()).isEqualTo(deviceType);
        assertThat(response.getIsActive()).isTrue();

        verify(fcmTokenRepository).findByToken(token);
        verify(fcmTokenRepository).save(captor.capture());

        FcmToken savedToken = captor.getValue();
        assertThat(savedToken.getUser().getId()).isEqualTo(userId);
        assertThat(savedToken.getToken()).isEqualTo(token);
        assertThat(savedToken.getDeviceType()).isEqualTo(deviceType);
        assertThat(savedToken.getIsActive()).isTrue();
    }

    @Test
    void registerToken_Success_WithExistingActiveToken() {
        // given
        Long userId = 1L;
        String token = "test-token";
        DeviceType deviceType = DeviceType.WEB;

        FcmToken existing = FcmToken.builder()
                .id(1L)
                .user(user)
                .token(token)
                .deviceType(deviceType)
                .isActive(true)
                .build();
        FcmToken spyExisting = spy(existing);

        FcmTokenDto.Request request = new FcmTokenDto.Request();
        ReflectionTestUtils.setField(request, "token", token);
        ReflectionTestUtils.setField(request, "deviceType", deviceType);

        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.of(spyExisting));
        when(fcmTokenRepository.save(spyExisting)).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FcmTokenDto.Response response = fcmTokenService.registerToken(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getDeviceType()).isEqualTo(deviceType);
        assertThat(response.getIsActive()).isTrue();

        verify(fcmTokenRepository).findByToken(token);
        verify(fcmTokenRepository).save(spyExisting);

        verify(spyExisting).refreshLastUsed();
    }

    @Test
    void registerToken_Success_WithInActiveToken_ActiveAndChangeUser() {
        // given
        Long userId = 2L;
        User newUser = User.builder().id(2L).build();
        String token = "test-token";
        DeviceType deviceType = DeviceType.WEB;

        FcmToken existing = FcmToken.builder()
                .id(1L)
                .user(user)
                .token(token)
                .deviceType(deviceType)
                .isActive(false)
                .build();
        FcmToken spyExisting = spy(existing);

        FcmTokenDto.Request request = new FcmTokenDto.Request();
        ReflectionTestUtils.setField(request, "token", token);
        ReflectionTestUtils.setField(request, "deviceType", deviceType);

        when(userRepository.getReferenceById(userId)).thenReturn(newUser);
        when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.of(spyExisting));
        when(fcmTokenRepository.save(spyExisting)).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FcmTokenDto.Response response = fcmTokenService.registerToken(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getDeviceType()).isEqualTo(deviceType);
        assertThat(response.getIsActive()).isTrue();

        verify(fcmTokenRepository).findByToken(token);
        verify(fcmTokenRepository).save(spyExisting);

        verify(spyExisting).changeUser(newUser);
        verify(spyExisting).activate();
    }

    @Test
    void deactivateToken_Success_withTokenExists() {
        // given
        String token = "test-token";
        FcmToken existing = FcmToken.builder()
                .id(1L)
                .user(user)
                .token(token)
                .isActive(false)
                .build();
        FcmToken spyExisting = spy(existing);

        FcmTokenDto.Request request = new FcmTokenDto.Request();
        ReflectionTestUtils.setField(request, "token", token);

        when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.of(spyExisting));

        // when
        fcmTokenService.deactivateToken(request);

        verify(fcmTokenRepository).findByToken(token);
        verify(spyExisting).deactivate();
    }
}