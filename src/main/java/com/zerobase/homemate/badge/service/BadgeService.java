package com.zerobase.homemate.badge.service;

import com.zerobase.homemate.badge.BadgeResponse;
import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.BadgeRepository;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.ChoreRepository;
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
    private final ChoreRepository choreRepository;
    private final CategoryChoreRepository categoryChoreRepository;
    private final UserRepository userRepository;
    private final UserBadgeStatsService userBadgeStatsService;


    // 집안일 완료 시 호출
    @Transactional
    public void evaluateBadges(User user){
        for(BadgeType badgeType : BadgeType.values()){
            if(badgeRepository.existsByUserAndBadgeType(user, badgeType)){
                continue;
            }

            Long completedCount = countCompleted(user, badgeType);
            if(completedCount >= badgeType.getRequireCount()){
                badgeRepository.save(new Badge(user, badgeType));
            }
        }
    }

    // 집안일 등록 시 호출
    @Transactional
    public void evaluateBadgesOnCreate(User user, Chore chore){
        List<Badge> badgesToSave = Arrays.stream(BadgeType.values())
                .filter(type -> !badgeRepository.existsByUserAndBadgeType(user, type))
                .filter(BadgeType::isRegisterBadge)
                .filter(type -> {
                    BadgeCondition condition = createCondition(type);
                    return condition != null && condition.matchesCondition(chore);
                })
                .map(type -> new Badge(user, type))
                .toList();

        if(!badgesToSave.isEmpty()){
            badgeRepository.saveAll(badgesToSave);
        }
    }

    public BadgeCondition createCondition(BadgeType type) {
        if(type.getSpace() != null){
            return new SpaceBadgeCondition(type.getSpace(), type.getRequireCount(), type.getBadgeName(), choreRepository);
        }


        if(type.getChoreTitle() != null){
            return new NameBadgeCondition(type.getChoreTitle(),  type.getRequireCount(), type.getBadgeName(), choreRepository);
        }

        return null;
    }

    // 유저의 획득한 배지 목록
    @Transactional(readOnly = true)
    public List<BadgeResponse> getAcquiredBadges(Long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<BadgeType> acquiredTypes = badgeRepository.findAllByUser(user)
                .stream()
                .map(Badge::getBadgeType)
                .toList();

        // 전체 배지를 순회하며 acquired 여부 체크
        List<BadgeResponse> allBadges = Arrays.stream(BadgeType.values())
                .map(type -> new BadgeResponse(
                        type,
                        acquiredTypes.contains(type),
                        countRemaining(user, type)
                ))
                .toList();

        // 획득한 배지 / 잠금 배지로 분리
        List<BadgeResponse> acquiredBadges = allBadges.stream()
                .filter(BadgeResponse::acquired)
                .sorted(Comparator.comparing(b -> b.type().getBadgeName()))
                .toList();

        List<BadgeResponse> lockedBadges = allBadges.stream()
                .filter(b -> !b.acquired())
                .sorted(Comparator.comparing(b -> b.type().getBadgeName()))
                .toList();

        // 합쳐서 반환 (획득 배지 먼저, 잠금 배지 나중)
        List<BadgeResponse> result = new ArrayList<>();
        result.addAll(acquiredBadges);
        result.addAll(lockedBadges);

        return result;
    }

    private int countRemaining(User user, BadgeType type) {

        // 이미 획득한 뱃지는 0으로 계산한다.
        if(badgeRepository.existsByUserAndBadgeType(user, type)){
            return 0;
        }

        Long completedCount = countCompleted(user, type);

        return Math.max(0, type.getRequireCount()) - completedCount.intValue();
    }

    // 아직 획득하지 못한 배지들 중 남은 횟수가 가장 적은 3개 리스트 반환
    @Transactional(readOnly = true)
    public List<BadgeResponse> getClosestBadges(Long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<BadgeType> acquiredTypes = badgeRepository.findAllByUser(user).stream()
                .map(Badge::getBadgeType).toList();

        return Arrays.stream(BadgeType.values())
                .filter(type -> !acquiredTypes.contains(type))
                .map(type -> {
                    Long completed = countCompleted(user, type);
                    int remaining = Math.max(0, type.getRequireCount()) - completed.intValue();
                    return new  BadgeResponse(type, false, remaining);
                })
                .sorted(Comparator.comparingInt(BadgeResponse::remainingCount))
                .limit(3)
                .toList();
    }

    // 완료된 집안일 카운트 계산
    private Long countCompleted(User user, BadgeType badgeType){
        if (badgeType.getSpace() != null) {
            return userBadgeStatsService.getSpaceCount(user.getId(), badgeType.getSpace().name());
        }

        if (badgeType.getChoreTitle() != null) {
            return userBadgeStatsService.getTitleCount(user.getId(), badgeType.getChoreTitle());
        }
        return userBadgeStatsService.getCount(user.getId());
    }
}
