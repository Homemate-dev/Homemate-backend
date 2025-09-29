package com.zerobase.homemate.recommend.controller;


import com.zerobase.homemate.entity.MonthlyCategoryTrend;
import com.zerobase.homemate.recommend.service.MonthlyCategoryTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class MonthlyTrendCategoryController {

    private final MonthlyCategoryTrendService trendService;

    @PostMapping
    public ResponseEntity<MonthlyCategoryTrend> createTrend(
            @RequestParam Long categoryId,
            @RequestParam String yyyymm,
            @RequestParam Long count
    ) {
        YearMonth month = YearMonth.parse(yyyymm); // "2025-09" 형태 기대
        return ResponseEntity.ok(trendService.createTrend(categoryId, month, count));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonthlyCategoryTrend> getTrend(@PathVariable Long id) {
        return ResponseEntity.ok(trendService.getTrend(id));
    }

    @GetMapping
    public ResponseEntity<List<MonthlyCategoryTrend>> getTrendsByMonth(@RequestParam String yyyymm) {
        YearMonth month = YearMonth.parse(yyyymm);
        return ResponseEntity.ok(trendService.getTrendsByMonth(month));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonthlyCategoryTrend> updateTrend(
            @PathVariable Long id,
            @RequestParam Long newCount
    ) {
        return ResponseEntity.ok(trendService.updateTrend(id, newCount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrend(@PathVariable Long id) {
        trendService.deleteTrend(id);
        return ResponseEntity.noContent().build();
    }
}
