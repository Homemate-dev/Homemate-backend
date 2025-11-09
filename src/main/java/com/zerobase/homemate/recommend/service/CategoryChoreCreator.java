package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.Category;
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
    public ApiResponse<List<ChoreInstanceDto.Response>> createChoreFromCategory(Long userId,
                                                                                Category category, Long categoryChoreId) {
        // 1. 사용자 유효성 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. CategoryChore(템플릿) 조회
        CategoryChore template = categoryChoreRepository.findById(categoryChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));


        // 3. 동일한 집안일 찾기 (title 기준)
        SpaceChore matchedSpaceChore = spaceChoreRepository.findByTitleKo(template.getTitle())
                .orElse(null);

        // 4. Space 결정: 매칭된 항목이 없으면 ETC
        Space space = (matchedSpaceChore != null) ? matchedSpaceChore.getSpace() : Space.ETC;

        // 5. 사용자 설정으로부터 Notification 여부, notification time의 기본 설정을 가져온다.
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(9, 0)));


        // 5. Chore 조회 또는 새로 생성
        Chore chore = choreRepository.findByUserIdAndTitle(userId, template.getTitle())
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

        // 6. 반복 인스턴스 생성 및 저장
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(chore);
        choreInstanceRepository.saveAll(instances);

        // 7. Redis counting 반영
        redisChoreStatsService.increment(template.getCategory(), space);

        // 8. 미션/뱃지 처리
        List<MissionDto.Response> userMission =
            missionService.increaseMissionCountForAction(userId,
                UserActionType.CREATE_CHORE_RECOMMENDED)
                .stream().filter(MissionDto.Response::isCompleted).toList();

        userBadgeStatsService.incrementRegisterCount(userId);

        // 9. DTO 변환 후 반환
        List<ChoreInstanceDto.Response> responseDtos = instances.stream()
                .map(ChoreInstanceDto.Response::fromEntity)
                .toList();

        return ApiResponse.<List<ChoreInstanceDto.Response>>builder()
                .data(responseDtos)
                .missionResults(userMission)
                .build();
    }



}
