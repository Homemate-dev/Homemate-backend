package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.badge.BadgeProgressResponse;
import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeCategory;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeStatsService userBadgeStatsService;

    private Map<BadgeType, BadgeCondition> conditionCache(){
        Map<BadgeType, BadgeCondition> badgeMap = new HashMap<>();

        for(BadgeType type : BadgeType.values()){
            switch(type.getCategory()){
                case MISSION -> badgeMap.put(type, new MissionBadgeCondition(type.getRequireCount(), userBadgeStatsService));
                case TITLE -> badgeMap.put(type, new NameBadgeCondition(type.getChoreTitle(), type.getRequireCount(), userBadgeStatsService));
                case SPACE -> badgeMap.put(type, new SpaceBadgeCondition(type.getSpace(), type.getRequireCount(), userBadgeStatsService));
                case ALL -> badgeMap.put(type, new TotalBadgeCondition(type.getRequireCount(), userBadgeStatsService));
                default -> {}
            }
        }
        return badgeMap;
    }

    /*
     미션 완료 시 배지 평가
      */
    @Transactional
    public void evaluateBadgesMission(User user){
        userBadgeStatsService.incrementMissionCount(user.getId());

        long currentCount = userBadgeStatsService.getTotalMissionCount(user.getId());

        List<Badge> badgesToSave = Arrays.stream(BadgeType.values())
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(type -> type.getCategory() == BadgeCategory.MISSION)
                .filter(type -> currentCount >= type.getRequireCount())
                .map(type -> new Badge(user, type))
                .toList();


        if(!badgesToSave.isEmpty()){
            badgeRepository.saveAll(badgesToSave);

            for (Badge b : badgesToSave) {
                log.info("Mission Badge Saved : type={}, acquiredAt={}", b.getBadgeType(), b.getAcquiredAt());
            }

        }
    }


    /*
    공간별, 이름별 집안일 완료 시 배지 평가
     */
    @Transactional
    public void evaluateBadges(User user, Chore chore) {

        userBadgeStatsService.incrementTotalCompleted(user.getId());
        if(chore.getSpace() != null) {
            userBadgeStatsService.incrementSpaceCount(user.getId(), chore.getSpace());
        }
        if(chore.getTitle() != null) {
            userBadgeStatsService.incrementTitleCount(user.getId(), chore.getTitle());
        }

        // DB에서 이미 획득한 Badge 조회
        Set<BadgeType> acquired = badgeRepository.findAllByUserId(user.getId())
                .stream().map(Badge::getBadgeType).collect(Collectors.toSet());

        Map<BadgeType, BadgeCondition> conditions = conditionCache();


        // Badge 평가 및 생성
        List<Badge> toSave = new ArrayList<>();
        for(Map.Entry<BadgeType, BadgeCondition> entry : conditions.entrySet()){
            BadgeType type = entry.getKey();
            BadgeCondition condition = entry.getValue();

            if(acquired.contains(type)) continue;

            if(condition.matchesCondition(chore)){
                toSave.add(new Badge(user, type));
            }
        }

        if(!toSave.isEmpty()){
            badgeRepository.saveAll(toSave);
            for (Badge b : toSave) {
                log.info("badge Saved : type={}, acquiredAt={}", b.getBadgeType(), b.getAcquiredAt());
            }

        }
    }

    // 집안일 등록 시 호출
    @Transactional
    public void evaluateBadgesOnCreate(User user){

        userBadgeStatsService.incrementTotalRegistered(user.getId());

        long currentRegisterCount = userBadgeStatsService.getTotalRegisteredCount(user.getId());

        List<Badge> badgesToSave = Arrays.stream(BadgeType.values())
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(type -> type.getCategory() == BadgeCategory.REGISTER)
                .filter(type -> currentRegisterCount >= type.getRequireCount())
                .map(type -> new Badge(user, type))
                .toList();

        if(!badgesToSave.isEmpty()){
            badgeRepository.saveAll(badgesToSave);
            for (Badge b : badgesToSave) {
                log.info("Registration Badge Saved : type={}, acquiredAt={}", b.getBadgeType(), b.getAcquiredAt());
            }

        }
    }


    @Transactional(readOnly = true)
    public List<BadgeProgressResponse> getAcquiredBadges(Long userId) {
        // 유저가 획득한 배지를 Set으로 가져오기
        Set<Badge> acquiredSet = new HashSet<>(badgeRepository.findAllByUserId(userId));
        log.info("getAcquiredBadges - userId: {}, acquiredSet.size: {}", userId, acquiredSet.size());

        List<BadgeProgressResponse> all = new ArrayList<>();
        for (BadgeType type : BadgeType.values()) {
            long currentCount = getCountByCategory(userId, type);

            // 획득 여부와 acquiredAt 확인
            Badge badge = acquiredSet.stream()
                    .filter(b -> b.getBadgeType() == type)
                    .findFirst()
                    .orElse(null);

            boolean acquired = badge != null;
            LocalDateTime acquiredAt = acquired ? badge.getAcquiredAt() : null;

            BadgeProgressResponse dto = BadgeProgressResponse.of(type, (int) currentCount, acquired, acquiredAt);
            all.add(dto);
        }

        all.sort(Comparator.comparing(BadgeProgressResponse::acquired).reversed()
                .thenComparing(BadgeProgressResponse::badgeTitle));

        return all;
    }


    // 아직 획득하지 못한 배지들 중 남은 횟수가 가장 적은 3개 리스트 반환
    @Transactional(readOnly = true)
    public List<BadgeProgressResponse> getClosestBadges(Long userId) {
        Map<BadgeType, Badge> acquiredMap = badgeRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(Badge::getBadgeType, b -> b));
        log.info("getClosestBadges - userId: {}, acquiredMap.size: {}", userId, acquiredMap.size());

        return Arrays.stream(BadgeType.values())
                .map(type -> {
                    int currentCount = (int) getCountByCategory(userId, type);
                    Badge badge = acquiredMap.get(type);
                    boolean isAcquired = badge != null;
                    LocalDateTime acquiredAt = isAcquired ? badge.getAcquiredAt() : null;

                    return BadgeProgressResponse.of(type, currentCount, isAcquired, acquiredAt);
                })
                .filter(b -> !b.acquired()) // 아직 획득하지 않은 배지만
                .sorted(Comparator.comparingInt(BadgeProgressResponse::remainingCount)) // 남은 횟수 적은 순
                .limit(3)
                .toList();
    }


    public long getCountByCategory(Long userId, BadgeType type) {
        return switch (type.getCategory()) {
            case ALL -> userBadgeStatsService.getTotalCompletedCount(userId);
            case REGISTER -> userBadgeStatsService.getTotalRegisteredCount(userId);
            case MISSION -> userBadgeStatsService.getTotalMissionCount(userId);
            case SPACE -> userBadgeStatsService.getSpaceCount(userId, type.getSpace());
            case TITLE -> userBadgeStatsService.getTitleCount(userId, type.getChoreTitle());
        };
    }


}
