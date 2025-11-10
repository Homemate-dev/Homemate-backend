package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.zerobase.homemate.util.ChoreDateUtils.calculateEndDate;

@Service
@RequiredArgsConstructor
public class SpaceChoreCreator {

    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final ChoreInstanceGenerator choreInstanceGenerator;
    private final SpaceChoreRepository spaceChoreRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final RedisChoreStatsService redisChoreStatsService;
    private final CategoryChoreRepository categoryChoreRepository;
    private final MissionService missionService;
    private final UserBadgeStatsService userBadgeStatsService;

    @Transactional
    public List<ChoreInstanceDto.Response> createChoreFromSpace(
            Long userId, Space space, Long spaceChoreId) {

        // 사용자 유효성 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        // SpaceChore 조회
        SpaceChore template = spaceChoreRepository.findById(spaceChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        // Chore 조회 또는 생성
        Chore chore = choreRepository.findByUserIdAndTitle(userId, template.getTitleKo())
                .orElseGet(() -> {
                    UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                            .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(19, 0)));

                    Chore newChore = Chore.builder()
                            .user(user)
                            .title(template.getTitleKo())
                            .space(template.getSpace())
                            .repeatType(template.getRepeatType())
                            .repeatInterval(template.getRepeatInterval())
                            .startDate(LocalDate.now())
                            .endDate(calculateEndDate(LocalDate.now(), template.getRepeatType(), template.getRepeatInterval()))
                            .notificationYn(setting.isChoreEnabled())
                            .notificationTime(setting.getNotificationTime())
                            .isDeleted(false)
                            .build();
                    return choreRepository.save(newChore);
                });

        // fetch join으로 안전하게 조회
        List<ChoreInstance> instances = choreInstanceRepository.findByChoreIdWithChore(
                chore.getId(), ChoreStatus.DELETED);

        // ChoreInstance가 없으면 생성
        if (instances.isEmpty()) {
            instances = choreInstanceGenerator.generateInstances(chore)
                    .stream()
                    .limit(1)
                    .toList();
            choreInstanceRepository.saveAll(instances);
        }

        // Category 소속 조회
        CategoryChore matchedCategoryChore = categoryChoreRepository.findByTitle(template.getTitleKo())
                .orElse(null);
        Category category = (matchedCategoryChore != null) ? matchedCategoryChore.getCategory() : Category.ETC;

        // Redis 통계 증가
        redisChoreStatsService.increment(category, template.getSpace());

        // 미션 / 뱃지 처리
        List<MissionDto.Response> userMission =
                missionService.increaseMissionCountForAction(userId, UserActionType.CREATE_CHORE_WITH_SPACE)
                        .stream()
                        .filter(MissionDto.Response::isCompleted)
                        .toList();
        userBadgeStatsService.incrementRegisterCount(userId);

        return instances.stream()
                .map(ChoreInstanceDto.Response::fromEntity)
                .toList();
    }

}
