package com.zerobase.homemate.notification.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.notification.push.dto.TokenWithIdDto;
import com.zerobase.homemate.repository.FcmTokenRepository;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmPushService {

    public static final int DB_BATCH_SIZE = 2000;
    public static final int FCM_BATCH_SIZE = 500;

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

    public void sendGlobal(String title, String message) {
        long lastId = 0L;

        while (true) {
            List<TokenWithIdDto> list = fcmTokenRepository.findIdAndTokenBatch(lastId, DB_BATCH_SIZE);

            List<String> sendBuffer = new ArrayList<>(FCM_BATCH_SIZE);
            for (TokenWithIdDto t : list) {
                sendBuffer.add(t.token());

                if (sendBuffer.size() >= FCM_BATCH_SIZE) {
                    sendBatch(sendBuffer, title, message);
                    sendBuffer.clear();
                }

                lastId = t.id();
            }

            if (!sendBuffer.isEmpty()) {
                sendBatch(sendBuffer, title, message);
            }

            if (list.size() < DB_BATCH_SIZE) {
                break;
            }
        }
    }

    public void sendBatch(List<String> tokens, String title, String message) {
        MulticastMessage fcmMessage = MulticastMessage.builder()
                .putData("title", title)
                .putData("body", message)
                .addAllTokens(tokens)
                .build();

        FirebaseMessaging instance = FirebaseMessaging.getInstance();
        instance.sendEachForMulticastAsync(fcmMessage);
    }
}
