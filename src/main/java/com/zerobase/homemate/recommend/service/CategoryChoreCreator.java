package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto;
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
    public ChoreDto.ApiResponse<List<ChoreInstanceDto.Response>> createChoreFromCategory(
            Long userId, Category category, Long categoryChoreId) {

        // 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // CategoryChore 템플릿 조회
        CategoryChore template = categoryChoreRepository.findById(categoryChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));



        // Space 결정
        SpaceChore matchedSpaceChore = spaceChoreRepository.findByTitleKo(template.getTitle())
                .orElse(null);
        Space space = (matchedSpaceChore != null) ? matchedSpaceChore.getSpace() : Space.ETC;

        // 알림 설정 불러오기
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserNotificationSetting defaultSetting =
                            UserNotificationSetting.createDefault(user, LocalTime.of(19, 0));
                    return userNotificationSettingRepository.save(defaultSetting);
                });


        // Chore 조회 또는 생성
        Chore chore = choreRepository.findByUserIdAndTitle(user.getId(), template.getTitle())
                .orElseGet(() -> {
                    Chore newChore = Chore.builder()
                            .user(user)
                            .title(template.getTitle())
                            .space(space)
                            .repeatType(template.getRepeatType())
                            .repeatInterval(template.getRepeatInterval())
                            .startDate(LocalDate.now())
                            .endDate(calculateEndDate(
                                    LocalDate.now(),
                                    template.getRepeatType(),
                                    template.getRepeatInterval()
                            ))
                            .notificationYn(setting.isChoreEnabled())
                            .notificationTime(setting.getNotificationTime())
                            .isDeleted(false)
                            .build();

                    return choreRepository.save(newChore);
                });

        // 인스턴스 생성 및 저장
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

        // 통계 업데이트
        redisChoreStatsService.increment(template.getCategory(), space);
        userBadgeStatsService.incrementRegisterCount(userId);

        List<MissionDto.Response> userMission =
                missionService.increaseMissionCountForAction(
                                userId, UserActionType.CREATE_CHORE_RECOMMENDED)
                        .stream().filter(MissionDto.Response::isCompleted).toList();

        // 응답 생성
        return ChoreDto.ApiResponse.<List<ChoreInstanceDto.Response>>builder()
                .data(instances.stream()
                        .map(ChoreInstanceDto.Response::fromEntity)
                        .toList())
                .missionResults(userMission)
                .build();
    }
}
