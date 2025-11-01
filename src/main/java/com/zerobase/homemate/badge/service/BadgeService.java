package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.badge.BadgeProgressResponse;
import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeCategory;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.BadgeRepository;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserBadgeStatsService userBadgeStatsService;


    // 집안일 완료 시 호출
    @Transactional
    public void evaluateBadges(User user, Chore chore) {
        List<Badge> badgesToSave = Arrays.stream(BadgeType.values())
                // 아직 획득하지 않은 배지만
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                // MISSION, TITLE, SPACE 배지만 필터링
                .filter(type -> type.getCategory() == BadgeCategory.MISSION
                        || type.getCategory() == BadgeCategory.TITLE
                        || type.getCategory() == BadgeCategory.SPACE)
                // 각 뱃지 조건 생성 후 충족 여부 검사
                .filter(type -> {
                    BadgeCondition condition = createCondition(type);
                    return condition != null && condition.matchesCondition(user, chore);
                })

                // 충족 시 배지 생성
                .map(type -> new Badge(user, type))
                .toList();
        evaluateAllBadges(user);

        if (!badgesToSave.isEmpty()) {
            badgeRepository.saveAll(badgesToSave);
        }
    }

    // 어떤 집안일이든 완료했을 때 호출
    public void evaluateAllBadges(User user) {
        List<Badge> allBadgesToSave = Arrays.stream(BadgeType.values())
                .filter(type -> type.getCategory() == BadgeCategory.ALL)
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .map(type -> new Badge(user, type))
                .toList();


        if (!allBadgesToSave.isEmpty()) {
            badgeRepository.saveAll(allBadgesToSave);
        }
    }


    // 집안일 등록 시 호출
    @Transactional
    public void evaluateBadgesOnCreate(User user, Chore chore){
        List<Badge> badgesToSave = Arrays.stream(BadgeType.values())
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(type -> type.getCategory() == BadgeCategory.REGISTER)
                .map(type -> new Badge(user, type))
                .toList();

        if(!badgesToSave.isEmpty()){
            badgeRepository.saveAll(badgesToSave);
        }
    }

    public BadgeCondition createCondition(BadgeType type) {
        return switch (type.getCategory()) {
            case MISSION -> new MissionBadgeCondition(type.getRequireCount(), type.getBadgeName(), userBadgeStatsService);
            case TITLE -> new NameBadgeCondition(type.getChoreTitle(), type.getRequireCount(), type.getBadgeName(), userBadgeStatsService);
            case SPACE -> new SpaceBadgeCondition(type.getSpace(), type.getRequireCount(), type.getBadgeName(), userBadgeStatsService);
            default -> null; // ALL, REGISTER
        };
    }

    // 유저의 획득한 배지 목록
    @Transactional(readOnly = true)
    public List<BadgeProgressResponse> getAcquiredBadges(Long userId) {

        List<BadgeProgressResponse> allBadges = Arrays.stream(BadgeType.values())
                .map(type -> {
                    int currentCount = (int) userBadgeStatsService.getCountByCategory(userId, type);
                    return BadgeProgressResponse.of(type, currentCount);
                })
                .toList();

        List<BadgeProgressResponse> acquiredBadges = allBadges.stream()
                .filter(BadgeProgressResponse::acquired)
                .sorted(Comparator.comparing(b -> b.badgeType().getBadgeName()))
                .toList();

        List<BadgeProgressResponse> lockedBadges = allBadges.stream()
                .filter(b -> !b.acquired())
                .sorted(Comparator.comparing(b -> b.badgeType().getBadgeName()))
                .toList();

        List<BadgeProgressResponse> result = new ArrayList<>();
        result.addAll(acquiredBadges);
        result.addAll(lockedBadges);

        return result;
    }

    // 아직 획득하지 못한 배지들 중 남은 횟수가 가장 적은 3개 리스트 반환
    @Transactional(readOnly = true)
    public List<BadgeProgressResponse> getClosestBadges(Long userId) {

        return Arrays.stream(BadgeType.values())
                .map(type -> {
                    int currentCount = (int) userBadgeStatsService.getCountByCategory(userId, type);
                    return BadgeProgressResponse.of(type, currentCount);
                })
                .filter(b -> !b.acquired()) // 아직 획득하지 않은 배지만
                .sorted(Comparator.comparingInt(BadgeProgressResponse::remainingCount)) // 남은 횟수 적은 순
                .limit(3)
                .toList();
    }

}
