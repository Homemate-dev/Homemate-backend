package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.badge.BadgeProgressResponse;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.BadgeCategory;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.entity.enums.TimeSlot;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.BadgeRepository;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
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
    private final BadgeCacheService badgeCacheService;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    private Map<BadgeType, BadgeCondition> conditionCache(){
        Map<BadgeType, BadgeCondition> badgeMap = new HashMap<>();

        for(BadgeType type : BadgeType.values()){
            switch(type.getCategory()){
                case MISSION -> badgeMap.put(type, new MissionBadgeCondition(type.getRequireCount(), userBadgeStatsService));
                case TITLE -> badgeMap.put(type, new NameBadgeCondition(type.getChoreTitle(), type.getRequireCount(), userBadgeStatsService));
                case SPACE -> badgeMap.put(type, new SpaceBadgeCondition(type.getSpace(), type.getRequireCount(), userBadgeStatsService));
                case ALL -> badgeMap.put(type, new TotalBadgeCondition(type.getRequireCount(), userBadgeStatsService));
                case TIME -> badgeMap.put(type, new TimeBadgeCondition(type.getTimeSlot(), type.getRequireCount(), userBadgeStatsService));
                case STREAK -> badgeMap.put(type, new StreakBadgeCondition(type.getRequireCount(), userBadgeStatsService));
                case ALARM -> badgeMap.put(type, new AlarmBadgeCondition());
                case ACCUMULATIVE -> badgeMap.put(type, new AccumulativeBadgeCondition());
                default -> {}
            }
        }
        return badgeMap;
    }

    /*
    Time(특정 시간대), Streak(연속 집안일 완료 일 수), Accumulative(알람 설정 변경 후 집안일 누적 상태) 평가 Method
     */
    @Transactional
    public void evaluateBadgesOnCompletion(User user, ChoreInstance choreInstance) {

        LocalDateTime completedAt = choreInstance.getCompletedAt();
        log.info("start evaluating when completion choreInstance : {}", choreInstance.getCompletedAt());

        updateTimeStats(user, completedAt);
        updateStreakStats(user, completedAt);

        List<Badge> badgesToGrant = new ArrayList<>();

        badgesToGrant.addAll(evaluateTimeBadges(user));
        badgesToGrant.addAll(evaluateStreakBadges(user));
        badgesToGrant.addAll(evaluateAccumulativeBadges(user));

        if (!badgesToGrant.isEmpty()) {
            badgeRepository.saveAll(badgesToGrant);
        }

        badgeCacheService.evictClosestBadges(user.getId());
    }

    private List<Badge> evaluateAccumulativeBadges(User user) {
        if (!userBadgeStatsService.hasChangedAlarm(user.getId())) {
            return List.of();
        }

        UserNotificationSetting setting = userNotificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_SETTING_NOT_FOUND));

        if(!setting.isChoreEnabled() && !setting.isMasterEnabled()){
            return List.of();
        }

        long count = userBadgeStatsService.increaseChoreCountAfterAlarm(user.getId());

        List<Badge> result = new ArrayList<>();

        List<BadgeType> accumulativeTypes = Arrays.stream(BadgeType.values())
                .filter(t -> t.getCategory() == BadgeCategory.ACCUMULATIVE)
                .sorted(Comparator.comparingInt(BadgeType::getRequireCount))
                .toList();

        for (BadgeType type : accumulativeTypes) {
            if (count < type.getRequireCount()) {
                continue;
            }

            if (badgeRepository.existsByUserAndBadgeType(user, type)) {
                continue;
            }

            result.add(new Badge(user, type));
        }

        return result;
    }

    private List<Badge> evaluateStreakBadges(User user) {
        return Arrays.stream(BadgeType.values())
                .filter(type -> type.getCategory() == BadgeCategory.STREAK)
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(type ->
                        userBadgeStatsService.getStreakCount(user.getId())
                                >= type.getRequireCount()
                )
                .map(type -> new Badge(user, type))
                .toList();
    }

    private List<Badge> evaluateTimeBadges(User user) {
        return Arrays.stream(BadgeType.values())
                .filter(type -> type.getCategory() == BadgeCategory.TIME)
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(type ->
                        userBadgeStatsService.getTimeCount(
                                user.getId(),
                                type.getTimeSlot()
                        ) >= type.getRequireCount()
                )
                .map(type -> new Badge(user, type))
                .toList();
    }

    private void updateStreakStats(User user, LocalDateTime completedAt) {
        userBadgeStatsService.updateStreak(
                user.getId(),
                completedAt.toLocalDate()
        );
    }

    private void updateTimeStats(User user, LocalDateTime completedAt) {
        TimeSlot slot = TimeSlot.from(completedAt);
        userBadgeStatsService.incrementTimeCount(user.getId(), slot);
    }

    /*
     미션 완료 시 배지 평가
      */
    @Transactional
    public void evaluateBadgesMission(User user){
        log.info("Start Mission Evaluating : {}", user.getId());
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
        log.info("start to evict mission Cache : {}", user.getId());
        badgeCacheService.evictClosestBadges(user.getId());
    }


    /*
    공간별, 이름별 집안일 완료 시 배지 평가
     */
    @Transactional
    public void evaluateBadges(User user, Chore chore) {
        log.info("start completion evaluating : {}", user.getId());

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

        log.info("start evict completion Cache : {}", user.getId());
        badgeCacheService.evictClosestBadges(user.getId());
    }

    // 집안일 등록 시 호출
    @Transactional
    public void evaluateBadgesOnCreate(User user){
        log.info("Start Create evaluating : {}", user.getId());
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

        log.info("Start evict Cache Register : {}",  user.getId());
        badgeCacheService.evictClosestBadges(user.getId());
    }

    @Transactional
    public void evaluateBadgesOnCategoryCreator(User user){
        log.info("Start Create Recommend Evaluation : {}", user.getId());
        userBadgeStatsService.increaseRecommendRegisterCount(user.getId());

        long currentCount = userBadgeStatsService.getRecommendRegisterCount(user.getId());

        List<Badge> badgesToSave = Arrays.stream(BadgeType.values())
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(type -> type.getCategory() == BadgeCategory.RECOMMEND_REGISTER)
                .filter(type -> currentCount >= type.getRequireCount())
                .map(type -> new Badge(user, type))
                .toList();

        if(!badgesToSave.isEmpty()){
            badgeRepository.saveAll(badgesToSave);
            for (Badge b : badgesToSave) {
                log.info("Recommend Registration Badge Saved : type={}, acquiredAt={}", b.getBadgeType(), b.getAcquiredAt());
            }
        }

        log.info("Start evict Cache Recommend Register : {}",  user.getId());
        badgeCacheService.evictClosestBadges(user.getId());
    }

    @Transactional
    public Optional<BadgeType> evaluateBadgesOnAlarm(User user){
        log.info("Start Alarm evaluating : {}", user.getId());

        if(badgeRepository.existsByUserAndBadgeType(user, BadgeType.ALARM_ALTER_START)){
            return Optional.empty();
        }

        boolean firstChanged =
                userBadgeStatsService.markAlarmChangedIfAbsent(user.getId());

        if (!firstChanged) {
            return Optional.empty();
        }

        Badge badge = Badge.builder()
                .user(user)
                .badgeType(BadgeType.ALARM_ALTER_START)
                .build();

        badgeRepository.save(badge);
        badgeCacheService.evictClosestBadges(user.getId());

        return Optional.of(badge.getBadgeType());
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
            case TIME -> userBadgeStatsService.getTimeCount(userId, type.getTimeSlot());
            case STREAK -> userBadgeStatsService.getStreakCount(userId);
            case ALARM -> userBadgeStatsService.hasChangedAlarm(userId) ? 1L : 0L;
            case ACCUMULATIVE -> userBadgeStatsService.getAccumulativeAfterAlarm(userId);
            case RECOMMEND_REGISTER -> userBadgeStatsService.getRecommendRegisterCount(userId);
        };
    }

    // Redis Caching Method (Front 호출 부탁)
    @Transactional(readOnly = true)
    public List<BadgeProgressResponse> getClosestBadgesCached(Long userId) {

        // 캐시 먼저 확인
        List<BadgeProgressResponse> cached = badgeCacheService.getCachedClosestBadges(userId);
        if (cached != null) {
            return cached;
        }
        log.info("[DEBUG] MISS → 계산 시작(userId={})", userId);

        // 캐시가 없으면 계산
        List<BadgeProgressResponse> computed = getClosestBadges(userId);
        log.info("[DEBUG] 계산 완료 → 결과 size={} (userId={})",
                computed == null ? -1 : computed.size(), userId);


        // 캐시 저장
        badgeCacheService.cacheClosestBadges(userId, computed);

        return computed;
    }


}
