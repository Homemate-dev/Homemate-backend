package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;

public class NameBadgeCondition implements BadgeCondition {

    private final String keyword;
    private final int requiredCount;
    private final String badgeName;
    private final UserBadgeStatsService userBadgeStatsService;

    public NameBadgeCondition(String keyword, int requiredCount, String badgeName,
                              UserBadgeStatsService userBadgeStatsService) {
        this.keyword = keyword;
        this.requiredCount = requiredCount;
        this.badgeName = badgeName;
        this.userBadgeStatsService = userBadgeStatsService;
    }

    @Override
    public boolean matchesCondition(Chore chore) {

        // 이 집안일은 해당 키워드와 관련이 있는가 확인
        if(chore.getTitle() == null || !chore.getTitle().equals(keyword)){
            return false;
        }

        // 사용자가 해당 키워드를 포함하는 집안일을 몇 번 완료했는지 카운트
        long completedCount = userBadgeStatsService.getTitleCount(chore.getUser().getId(), keyword);
        return completedCount >= requiredCount;
    }

    @Override
    public String getBadgeName() {
        return badgeName;
    }
}
