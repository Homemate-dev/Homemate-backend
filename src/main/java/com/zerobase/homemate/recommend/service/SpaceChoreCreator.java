package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
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
    public ApiResponse<ChoreDto.Response> createChoreFromSpace(Long userId,
        Space space, Long spaceChoreId){
        // 1. 사용자 유효성 검증
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. SpaceChore 조회
        SpaceChore template = spaceChoreRepository.findById(spaceChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        // 3. 동일한 사용자가 이미 등록한 집안일인지 검증
        if(choreRepository.existsByUserIdAndTitle(userId, template.getTitleKo())){
            throw new CustomException(ErrorCode.CHORE_ALREADY_REGISTERED);
        }

        // 4. 사용자 설정으로부터 Notification 여부, notification time의 Chore에 대한 기본 설정을 가져오기
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(9, 0)));

        // 5. Chore 생성
        Chore chore = Chore.builder()
                .user(user)
                .title(template.getTitleKo())
                .space(template.getSpace())
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

        Chore saved = choreRepository.save(chore);

        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(saved);
        choreInstanceRepository.saveAll(instances);

        //. Category 소속여부 조회
        CategoryChore matchedCategoryChore = categoryChoreRepository.findByTitle(template.getTitleKo())
                        .orElse(null);

        if (matchedCategoryChore != null) {
            Category category = matchedCategoryChore.getCategory();
            redisChoreStatsService.increment(category, template.getSpace());
        }


        List<MissionDto.Response> userMission =
            missionService.increaseMissionCountForAction(userId,
            UserActionType.CREATE_CHORE_WITH_SPACE)
                .stream().filter(MissionDto.Response::isCompleted).toList();

        userBadgeStatsService.incrementRegisterCount(userId);

        return ApiResponse.<ChoreDto.Response>builder()
            .data(ChoreDto.Response.fromEntity(saved))
            .missionResults(userMission)
            .build();
    }

}
