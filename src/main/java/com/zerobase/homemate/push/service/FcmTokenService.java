package com.zerobase.homemate.push.service;

import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.DeviceType;
import com.zerobase.homemate.push.dto.FcmTokenDto;
import com.zerobase.homemate.repository.FcmTokenRepository;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public FcmTokenDto.Response registerToken(Long userId, FcmTokenDto.Request request) {
        User user = userRepository.getReferenceById(userId);
        String requestToken = request.getToken();
        DeviceType deviceType = request.getDeviceType();

        FcmToken token = fcmTokenRepository.findByToken(requestToken)
                .map(existing -> {
                    // 사용자 변경(예: 동일 토큰이 다른 계정으로 넘어온 경우) 시 갱신
                    if (!user.equals(existing.getUser())) {
                        existing.changeUser(user);
                    }

                    // 디바이스 타입 변경 시 갱신
                    if (!Objects.equals(existing.getDeviceType(), deviceType)) {
                        existing.changeDeviceType(deviceType);
                    }

                    // 활성화 및 lastUsed 갱신
                    if (!Boolean.TRUE.equals(existing.getIsActive())) {
                        existing.activate();
                    } else {
                        existing.refreshLastUsed();
                    }

                    return existing;
                })
                .orElseGet(() -> {
                    FcmToken newToken = FcmToken.builder()
                            .user(user)
                            .token(requestToken)
                            .deviceType(deviceType)
                            .build();
                    newToken.activate();
                    return newToken;
                });

        FcmToken saved = fcmTokenRepository.save(token);

        return FcmTokenDto.Response.fromEntity(saved);
    }

    @Transactional
    public void deActivateToken(FcmTokenDto.Request request) {
        fcmTokenRepository.findByToken(request.getToken()).ifPresent(FcmToken::deactivate);
    }
}
