package com.zerobase.homemate.util.withdrawlogexporter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class WithdrawLogExportScheduler {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 1, 5);
    private final WithdrawLogBatchService batchService;

    @Value("${auth.dev.enabled}")
    private boolean devEnabled;

    @Scheduled(cron = "0 0 1 * * MON")
    public void runBiWeekly() {
        // 테스트 환경에서는 로그 미추출
        if (devEnabled) {
            return;
        }

        if (!isBiWeekly()) {
            return;
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusWeeks(2);

        batchService.executeBatchProcess(start, end);
    }

    private boolean isBiWeekly() {
        LocalDate today = LocalDate.now();
        long between = ChronoUnit.WEEKS.between(REFERENCE_DATE, today);
        return between % 2 == 0;
    }
}
