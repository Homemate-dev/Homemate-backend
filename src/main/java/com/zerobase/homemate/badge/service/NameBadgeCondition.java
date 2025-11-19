package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;

public class NameBadgeCondition implements BadgeCondition {

    private final String keyword;
    private final int requiredCount;
    private final UserBadgeStatsService userBadgeStatsService;

    public NameBadgeCondition(String keyword, int requiredCount,
                              UserBadgeStatsService userBadgeStatsService) {
        this.keyword = keyword;
        this.requiredCount = requiredCount;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(User user, Chore chore) {

        // 이 집안일은 해당 키워드와 관련이 있는가 확인
        if(chore.getTitle() == null || !chore.getTitle().equals(keyword)){
            return false;
        }
        // 사용자가 해당 키워드를 포함하는 집안일을 몇 번 완료했는지 카운트
        long completedCount = userBadgeStatsService.getTitleCount(chore.getUser().getId(), keyword);
        return completedCount >= requiredCount;
    }
}
