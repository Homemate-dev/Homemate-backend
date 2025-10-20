package com.zerobase.homemate.notification.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;

    public void send(User user, String title, String message) {
        List<FcmToken> list = fcmTokenRepository.findAllByUserAndIsActiveTrue(user);
        List<String> tokens = list.stream().map(FcmToken::getToken).toList();

        if (tokens.isEmpty()) {
            log.warn("No valid FCM tokens for userId={}", user.getId());
            return;
        }

        sendBatch(tokens, title, message);
    }

    public void sendBatch(List<String> tokens, String title, String message) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build();

        MulticastMessage fcmMessage = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(tokens)
                .build();

        FirebaseMessaging instance = FirebaseMessaging.getInstance();
        instance.sendEachForMulticastAsync(fcmMessage);
    }
}
