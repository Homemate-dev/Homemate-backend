package com.zerobase.homemate.notification.service;

import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.notification.component.ChoreInstanceCreatedEvent;
import com.zerobase.homemate.notification.component.ChoreNotificationCreateJob;
import com.zerobase.homemate.notification.model.NotificationIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChoreNotificationSchedulerService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final Scheduler scheduler;

    public void scheduleChoreNotification(ChoreInstanceCreatedEvent event) throws SchedulerException {
        Long userId = event.getUserId();
        Long instanceId = event.getChoreInstanceId();
        if (event.getScheduledAt() == null) {
            log.warn("Not scheduled choreInstance - userId = {}, instanceId = {}", userId, instanceId);
            return;
        }
        ZonedDateTime scheduledAt = event.getScheduledAt().atZone(ZONE);
        List<NotificationTime> times = NotificationTime.buildList(scheduledAt, event.getRepeatType());

        JobKey jk = jobKey(instanceId);
        if (!scheduler.checkExists(jk)) {
            JobDataMap jobMap = new JobDataMap();
            jobMap.put("userId", userId);
            jobMap.put("choreInstanceId", instanceId);
            JobDetail jd = JobBuilder.newJob(ChoreNotificationCreateJob.class)
                    .withIdentity(jk)
                    .usingJobData(jobMap)
                    .storeDurably(true)
                    .build();
            scheduler.addJob(jd, false);
        }

        for (NotificationTime time : times) {
            String index = time.notificationIndex().index();
            ZonedDateTime when = time.when();

            if (when.isBefore(ZonedDateTime.now(ZONE))) {
                // skip
                continue;
            }

            TriggerKey tk = triggerKey(instanceId, index);
            JobDataMap triggerMap = new JobDataMap();
            triggerMap.put("index", index);
            triggerMap.put("scheduledAt", when.toInstant().toEpochMilli());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(tk)
                    .forJob(jk)
                    .usingJobData(triggerMap)
                    .startAt(Date.from(when.toInstant()))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withMisfireHandlingInstructionFireNow()
                    )
                    .build();

            // 스케줄 존재하면 reschedule
            if (scheduler.checkExists(tk)) {
                Date next = scheduler.rescheduleJob(tk, trigger);
                log.info("Rescheduled trigger {} -> next fire: {}", tk, next);
            } else {
                scheduler.scheduleJob(trigger);
                log.info("Scheduled trigger {} for choreInstance {}", tk, instanceId);
            }
        }
    }

    private record NotificationTime(NotificationIndex notificationIndex, ZonedDateTime when) {
        private static List<NotificationTime> buildList(ZonedDateTime scheduledAt, RepeatType repeatType) {
            List<NotificationTime> list = new ArrayList<>();

            // 매달/3개월/6개월/1년의 경우 전날 알림 추가
            if (repeatType.equals(RepeatType.MONTHLY) || repeatType.equals(RepeatType.YEARLY)) {
                list.add(new NotificationTime(NotificationIndex.T_MINUS_1D, scheduledAt.minusDays(1)));
            }
            list.add(new NotificationTime(NotificationIndex.T_MINUS_10M, scheduledAt.minusMinutes(10)));
            list.add(new NotificationTime(NotificationIndex.T_0, scheduledAt));

            return list;
        }
    }

    private JobKey jobKey(Long choreInstanceId) {
        return JobKey.jobKey("choreInstance-" + choreInstanceId, "choreNotifications");
    }

    private TriggerKey triggerKey(Long choreInstanceId, String idx) {
        return TriggerKey.triggerKey("choreInstance-" + choreInstanceId + "-" + idx, "choreNotifications");
    }
}
