package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.RegistrationType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.notification.component.ChoreInstanceCreatedEvent;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChoreDto.ApiResponse<ChoreDto.Response> createChoreFromCategory(Long userId,
                                                                           Long categoryChoreId) {
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
                .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(19, 0)));

        // 4. Chore 생성
        Chore chore = Chore.builder()
                .user(user)
                .title(template.getTitle())
                .space(space) // 매핑된 Space 또는 ETC
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
                .registrationType(RegistrationType.CATEGORY)
                .build();

        // 5. 저장
        Chore saved = choreRepository.save(chore);

        // 알람 활성화 여부 조회 및 활성화
        if(chore.getNotificationYn()){
            userNotificationSettingRepository
                    .enableUserNotificationSetting(chore.getUser().getId());
        }

        // 6. 반복 인스턴스 생성
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(saved);
        choreInstanceRepository.saveAll(instances);

        for(ChoreInstance instance : instances){
            eventPublisher.publishEvent(ChoreInstanceCreatedEvent.create(chore.getUser().getId(),
                    instance,
                    chore.getNotificationTime(),
                    chore.getRepeatType()));
        }

        // 7. Redis counting 반영
        redisChoreStatsService.increment(template.getCategory(), space);

        List<MissionDto.Response> userMission =
                missionService.increaseMissionCountForAction(userId,
                                UserActionType.CREATE_CHORE_RECOMMENDED)
                        .stream().filter(MissionDto.Response::isCompleted).toList();

        userBadgeStatsService.incrementRegisterCount(userId);

        return ChoreDto.ApiResponse.<ChoreDto.Response>builder()
                .data(ChoreDto.Response.fromEntity(saved))
                .missionResults(userMission)
                .build();
    }
}
