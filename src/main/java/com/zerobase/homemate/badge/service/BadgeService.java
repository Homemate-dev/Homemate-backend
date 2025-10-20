package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.badge.BadgeResponse;
import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.repository.BadgeRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final ChoreRepository choreRepository;

    // 집안일 완료 시 호출
    @Transactional
    public void evaluateBadges(User user){
        for(BadgeType badgeType : BadgeType.values()){
            if(badgeRepository.existsByUserAndBadgeType(user, badgeType)){
                continue;
            }

            Long completedCount = countCompleted(user, badgeType);
            if(completedCount >= badgeType.getRequiredCount()){
                badgeRepository.save(new Badge(user, badgeType));
            }
        }
    }

    // 유저의 획득한 배지 목록
    @Transactional(readOnly = true)
    public List<BadgeResponse> getAcquiredBadges(User user){
        return badgeRepository.findAllByUser(user).stream()
                .map(b -> new BadgeResponse(b.getBadgeType(), true, 0))
                .toList();
    }

    // 아직 획득하지 못한 배지들 중 남은 횟수가 가장 적은 3개 리스트 반환
    @Transactional(readOnly = true)
    public List<BadgeResponse> getClosestBadges(User user){
        List<BadgeType> acquiredTypes = badgeRepository.findAllByUser(user).stream()
                .map(Badge::getBadgeType).toList();

        return Arrays.stream(BadgeType.values())
                .filter(type -> !acquiredTypes.contains(type))
                .map(type -> {
                    Long completed = countCompleted(user, type);
                    int remaining = Math.max(0, type.getRequiredCount() - (int) completed);
                    return new  BadgeResponse(type, false, remaining);
                })
                .sorted(Comparator.comparingInt(BadgeResponse::remainingCount))
                .limit(3)
                .toList();
    }

    // 완료된 집안일 카운트 계산
    private Long countCompleted(User user, BadgeType badgeType){
        if(badgeType.getSpace() != null){
            return choreRepository.countByUserAndSpaceAndIsCompletedTrue((user, badgeType.getSpace());
        }
        if(badgeType.getCategory() != null){
            return choreRepository.countByUserAndCategoryAndIsCompletedTrue((user, badgeType.getCategory());
        }

        if(badgeType.getChoreTitle() != null){
            return choreRepository.countByUserAndTitleAndIsCompletedTrue((user, badgeType.getChoreTitle());
        }
        return 0L;
    }
}
