package com.zerobase.homemate.notification.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.notification.push.dto.TokenWithIdDto;
import com.zerobase.homemate.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmPushService {

    public static final int DB_BATCH_SIZE = 2000;
    public static final int FCM_BATCH_SIZE = 500;

    @Value("${frontend.domain}")
    private String domain;

    private final FcmTokenRepository fcmTokenRepository;

    public void sendNotificationPush(User user, String title, String message, String taskType) {
        List<FcmToken> list = fcmTokenRepository.findAllByUserAndIsActiveTrue(user);
        List<String> tokens = list.stream().map(FcmToken::getToken).toList();

        if (tokens.isEmpty()) {
            log.warn("No valid FCM tokens for userId={}", user.getId());
            return;
        }

        String url = buildUrl(taskType);
        Map<String, String> dataMap = Map.of("title", title, "message", message, "url", url);

        sendBatch(tokens, dataMap);
    }

    public void sendGlobal(String title, String message) {
        Map<String, String> dataMap = Map.of("title", title, "message", message);
        long lastId = 0L;

        while (true) {
            List<TokenWithIdDto> list = fcmTokenRepository.findIdAndTokenBatch(lastId, PageRequest.of(0, DB_BATCH_SIZE));

            List<String> sendBuffer = new ArrayList<>(FCM_BATCH_SIZE);
            for (TokenWithIdDto t : list) {
                sendBuffer.add(t.token());

                if (sendBuffer.size() >= FCM_BATCH_SIZE) {
                    sendBatch(sendBuffer, dataMap);
                    sendBuffer.clear();
                }

                lastId = t.id();
            }

            if (!sendBuffer.isEmpty()) {
                sendBatch(sendBuffer, dataMap);
            }

            if (list.size() < DB_BATCH_SIZE) {
                break;
            }
        }
    }

    public void sendBatch(List<String> tokens, Map<String, String> dataMap) {
        MulticastMessage fcmMessage = MulticastMessage.builder()
                .putAllData(dataMap)
                .addAllTokens(tokens)
                .build();

        FirebaseMessaging instance = FirebaseMessaging.getInstance();
        instance.sendEachForMulticastAsync(fcmMessage);
    }

    private String buildUrl(String taskType) {
        return UriComponentsBuilder.fromUriString(domain)
                .queryParam("from_push", 1)
                .queryParam("task_type", taskType)
                .toUriString();
    }
}
