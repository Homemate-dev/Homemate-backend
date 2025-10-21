package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.repository.ChoreRepository;

public class SpaceBadgeCondition implements BadgeCondition {

    private final Space targetSpace;
    private final int requiredCount;
    private final String badgeName;
    private final ChoreRepository choreRepository;

    public SpaceBadgeCondition(Space targetSpace, int requiredCount, String badgeName, ChoreRepository choreRepository) {
        this.targetSpace = targetSpace;
        this.requiredCount = requiredCount;
        this.badgeName = badgeName;
        this.choreRepository = choreRepository;
    }

    @Override
    public boolean matchesCondition(Chore chore) {

        if(chore.getSpace() != targetSpace) {
            return false;
        }

        Long completedCount = choreRepository.countByUserAndSpaceAndIsCompletedTrue(chore.getUser(), targetSpace);

        return completedCount >= requiredCount;
    }

    @Override
    public String getBadgeName() {
        return badgeName;
    }
}
