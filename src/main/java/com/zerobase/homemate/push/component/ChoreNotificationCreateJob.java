package com.zerobase.homemate.push.component;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.notification.dto.ChoreNotificationCreateDto;
import com.zerobase.homemate.notification.service.NotificationService;
import com.zerobase.homemate.push.service.FcmPushService;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@DisallowConcurrentExecution
@Slf4j
@RequiredArgsConstructor
public class ChoreNotificationCreateJob extends QuartzJobBean {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final NotificationService notificationService;
    private final FcmPushService fcmPushService;

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long choreInstanceId = jobDataMap.getLong("choreInstanceId");
        Long userId = jobDataMap.getLong("userId");
        long scheduledAtMillis = jobDataMap.getLong("scheduledAt");
        ZonedDateTime scheduledAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(scheduledAtMillis), ZONE);

        log.info("Run ChoreNotificationJob: choreInstanceId={}, scheduledAt={}",
                choreInstanceId, scheduledAt);

        // 1. User 검증
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getUserStatus().equals(UserStatus.DELETED)) {
            log.error("Run ChoreNotificationJob: user not exists - userId={}, choreInstanceId={}", userId, choreInstanceId);
            cleanupAllTriggersAndJob(context);
            return;
        }

        // 2. ChoreInstance 검증
        ChoreInstance choreInstance = choreInstanceRepository.findById(choreInstanceId).orElse(null);
        if (choreInstance == null || choreInstance.getChoreStatus().equals(ChoreStatus.DELETED)) {
            log.warn("Run ChoreNotificationJob: choreInstance not exists - userId={}, choreInstanceId={}", userId, choreInstanceId);
            cleanupAllTriggersAndJob(context);
            return;
        } else if (choreInstance.getChoreStatus().equals(ChoreStatus.CANCELLED)) {
            log.warn("Run ChoreNotificationJob: choreInstance already cancelled - userId={}, choreInstanceId={}", userId, choreInstanceId);
            cleanupAllTriggersAndJob(context);
            return;
        } else if (choreInstance.getChoreStatus().equals(ChoreStatus.COMPLETED)) {
            log.info("Run ChoreNotificationJob: choreInstance already completed - userId={}, choreInstanceId={}", userId, choreInstanceId);
            // COMPLETED 상태의 경우 다시 PENDING으로 바뀔 수 있으므로 현재 트리거만 취소
            cleanupCurrentTriggerOnly(context);
            return;
        }

        // 3. ChoreNotification 생성 (인앱 수신함)
        String title = choreInstance.getTitleSnapshot();
        String message = ""; // TODO: 메시지 템플릿 질의

        try {
            ChoreNotificationCreateDto request = ChoreNotificationCreateDto.builder()
                    .userId(userId)
                    .choreInstanceId(choreInstanceId)
                    .title(title)
                    .message(message)
                    .scheduledAt(scheduledAt.toLocalDateTime())
                    .build();

            notificationService.createChoreNotification(request);
        } catch (Exception e) {
            JobExecutionException ex = new JobExecutionException(e);
            ex.setRefireImmediately(true);
            throw ex;
        }

        // 4. FCM Push 발송
        try {
            fcmPushService.send(user, title, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void cleanupAllTriggersAndJob(JobExecutionContext context) {
        try {
            Scheduler scheduler = context.getScheduler();
            JobKey jobKey = context.getJobDetail().getKey();

            // 모든 트리거 언스케줄
            for (Trigger t : scheduler.getTriggersOfJob(jobKey)) {
                TriggerKey key = t.getKey();
                try {
                    boolean uns = scheduler.unscheduleJob(key);
                    log.info("Unschedule trigger {} -> {}", key, uns);
                } catch (SchedulerException se) {
                    log.warn("Failed to unschedule trigger {}", key, se);
                }
            }

            // Job 삭제
            boolean deleted = scheduler.deleteJob(jobKey);
            log.info("Deleted job {} result={}", jobKey, deleted);
        } catch (SchedulerException e) {
            log.error("Failed to cleanup all scheduling", e);
        }
    }

    private void cleanupCurrentTriggerOnly(JobExecutionContext context) {
        try {
            Scheduler scheduler = context.getScheduler();
            TriggerKey currentTriggerKey = context.getTrigger().getKey();

            boolean uns = scheduler.unscheduleJob(currentTriggerKey);
            log.info("Unschedule trigger {} -> {}", currentTriggerKey, uns);
        } catch (SchedulerException e) {
            log.warn("Failed to unschedule current trigger", e);
        }
    }
}
