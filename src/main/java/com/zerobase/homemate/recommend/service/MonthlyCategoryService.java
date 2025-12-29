package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class MonthlyCategoryService {

    private final CategoryChoreRepository categoryChoreRepository;

    @Transactional
    public void refreshMonthlyCategories() {
        YearMonth current = YearMonth.now(ZoneId.of("Asia/Seoul"));
        YearMonth previous = current.minusMonths(1);

        deactivate(previous);
        activate(current);
    }

    private void deactivate(YearMonth yearMonth) {
        categoryChoreRepository.deactivateAllMonthly(
                CategoryType.MONTHLY,
                yearMonth.toString()
        );
    }

    private void activate(YearMonth yearMonth) {
        categoryChoreRepository.activateMonthly(
                CategoryType.MONTHLY,
                yearMonth.toString()
        );
    }
}
