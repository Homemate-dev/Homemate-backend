package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.entity.MonthlyCategoryTrend;
import com.zerobase.homemate.repository.MonthlyCategoryTrendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonthlyCategoryTrendService {

    private final MonthlyCategoryTrendRepository trendRepository;
    private final CategoryService categoryService;

    @Transactional
    public MonthlyCategoryTrend createTrend(Long categoryId, YearMonth month, Long count) {
        Category category = categoryService.getCategory(categoryId);
        MonthlyCategoryTrend trend = MonthlyCategoryTrend.builder()
                .category(category)
                .yyyymm(month) // yyyymm -> month로 치환하는 로직이 필요하네!
                .count(count)
                .build();

        return trendRepository.save(trend);
    }

    public List<MonthlyCategoryTrend> getTrendsByMonth(YearMonth month) {
        return trendRepository.findByMonth(month);
    }

    public MonthlyCategoryTrend getTrend(Long id) {
        return trendRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Not Trend Found :" + id) // CustomException으로 변경 예정!
                );
    }

    public MonthlyCategoryTrend updateTrend(Long id, Long newCount){
        MonthlyCategoryTrend trend = getTrend(id);
        trend.updateCount(newCount);
        return trend;
    }

    @Transactional
    public void deleteTrend(Long id) {
        trendRepository.deleteById(id);
    }
}
