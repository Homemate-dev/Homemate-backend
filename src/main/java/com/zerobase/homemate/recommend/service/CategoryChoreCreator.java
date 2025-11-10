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
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.zerobase.homemate.util.ChoreDateUtils.calculateEndDate;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryChoreCreator {

    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final ChoreInstanceGenerator choreInstanceGenerator;
    private final CategoryChoreRepository categoryChoreRepository;
    private final SpaceChoreRepository spaceChoreRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final RedisChoreStatsService redisChoreStatsService;
    private final MissionService missionService;
    private final UserBadgeStatsService userBadgeStatsService;

    @Transactional
    public List<ChoreInstanceDto.Response> createChoreFromCategory(Long userId,
                                                                       Category category,
                                                                       Long categoryChoreId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        CategoryChore template = categoryChoreRepository.findById(categoryChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        SpaceChore matchedSpaceChore = spaceChoreRepository.findByTitleKo(template.getTitle())
                .orElse(null);
        Space space = (matchedSpaceChore != null) ? matchedSpaceChore.getSpace() : Space.ETC;

        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(9, 0)));

        Chore chore = choreRepository.findByUserIdAndTitleAndSpace(userId, template.getTitle(), space)
                .orElseGet(() -> {
                    Chore newChore = Chore.builder()
                            .user(user)
                            .title(template.getTitle())
                            .space(space)
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

        if (instances.isEmpty()) {
            instances = choreInstanceGenerator.generateInstances(chore)
                    .stream()
                    .limit(1)
                    .toList();
            choreInstanceRepository.saveAll(instances);
        }

        redisChoreStatsService.increment(template.getCategory(), space);
        missionService.increaseMissionCountForAction(userId, UserActionType.CREATE_CHORE_RECOMMENDED);
        userBadgeStatsService.incrementRegisterCount(userId);

        return instances.stream()
                .map(ChoreInstanceDto.Response::fromEntity)
                .toList();
    }




}
