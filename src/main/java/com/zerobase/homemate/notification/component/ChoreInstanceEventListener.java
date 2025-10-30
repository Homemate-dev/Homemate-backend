package com.zerobase.homemate.notification.component;

import com.zerobase.homemate.notification.service.ChoreNotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChoreInstanceEventListener {

    private final ChoreNotificationSchedulerService schedulerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(ChoreInstanceCreatedEvent event) {
        try {
            schedulerService.scheduleChoreNotification(event);
        } catch (SchedulerException e) {
            // TODO: 예외 처리 구성
        }
    }
}
