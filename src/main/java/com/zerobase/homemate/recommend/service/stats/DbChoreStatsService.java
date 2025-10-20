package com.zerobase.homemate.recommend.service.stats;


import com.zerobase.homemate.entity.ChoreStats;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.repository.ChoreStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DbChoreStatsService {

    private final ChoreStatsRepository choreStatsRepository;

    @Transactional
    public void syncFromRedis(Map<String, Long> categoryStats, Map<String, Long> spaceStats) {
        // 카테고리별 반영
        for(Map.Entry<String, Long> entry : categoryStats.entrySet()) {
            Category category = Category.valueOf(entry.getKey());
            Long count = entry.getValue();
            ChoreStats stat = choreStatsRepository.findByCategory(category)
                    .orElseGet(() -> {
                        ChoreStats s = ChoreStats.builder()
                                .category(category)
                                .count(0L)
                                .build();
                        choreStatsRepository.save(s);
                        return s;
                    });

            stat.increment(count);
        }

        // 공간별 반영
        for(Map.Entry<String, Long> entry : spaceStats.entrySet()) {
            Space space = Space.valueOf(entry.getKey());
            Long count = entry.getValue();
            ChoreStats stat = choreStatsRepository.findBySpace(space)
                    .orElseGet(() -> {
                        ChoreStats s = ChoreStats.builder()
                                .space(space)
                                .count(0L)
                                .build();
                        choreStatsRepository.save(s);
                        return s;
                    });
            stat.increment(count);
        }

    }
}
