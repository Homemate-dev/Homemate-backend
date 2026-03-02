package com.zerobase.homemate.recommend.component;

import com.zerobase.homemate.recommend.service.MonthlyCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyCategoryRefresher {

    private final MonthlyCategoryService monthlyCategoryService;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    @Transactional
    public void refreshMonthlyCategory(){
        monthlyCategoryService.refreshMonthlyCategories();
    }
}
