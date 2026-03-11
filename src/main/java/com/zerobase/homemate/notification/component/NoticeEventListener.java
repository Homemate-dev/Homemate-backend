package com.zerobase.homemate.notification.component;

import com.zerobase.homemate.notification.push.service.FcmPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NoticeEventListener {

    private final FcmPushService fcmPushService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(NoticeCreatedEvent event) {
        String title = event.getTitle();
        String message = event.getMessage();

        fcmPushService.sendGlobal(title, message);
    }
}
