package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.RegistrationType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.zerobase.homemate.util.ChoreDateUtils.calculateEndDate;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final ApplicationEventPublisher eventPublisher;
    private final BadgeService badgeService;

    @Transactional
    public ChoreDto.ApiResponse<ChoreDto.Response> createChoreFromSpace(Long userId,
                                                                        Long spaceChoreId){
        // 1. 사용자 유효성 검증
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. SpaceChore 조회
        SpaceChore template = spaceChoreRepository.findById(spaceChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        // 4. 사용자 설정으로부터 Notification 여부, notification time의 Chore에 대한 기본 설정을 가져오기
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(19, 0)));

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
                .registrationType(RegistrationType.SPACE)
                .build();

        boolean isDuplicate = choreRepository
                .existsByUserIdAndTitleAndRegistrationTypeInAndIsDeletedFalse(
                        userId,
                        chore.getTitle(),
                        List.of(RegistrationType.CATEGORY, RegistrationType.SPACE)
                );
        log.info(
                "[CREATE_SPACE_CHORE] userId={}, title='{}', isDuplicate={}, registrationType={}",
                userId,
                template.getTitleKo(),
                isDuplicate,
                chore.getRegistrationType()
        );

        Chore saved = choreRepository.save(chore);

        if(chore.getNotificationYn()){
            userNotificationSettingRepository
                    .enableUserNotificationSetting(chore.getUser().getId());
        }

        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(saved);
        choreInstanceRepository.saveAll(instances);

        List<CategoryChore> matchedCategoryChores =
                categoryChoreRepository.findAllByTitle(template.getTitleKo());

        if (matchedCategoryChores.isEmpty()) {
            log.debug(
                    "[ChoreStats] No CategoryChore found. title={}, space={}",
                    template.getTitleKo(),
                    template.getSpace()
            );
        } else {
            log.info(
                    "[ChoreStats] Increment stats. title={}, categories={}, space={}",
                    template.getTitleKo(),
                    matchedCategoryChores.stream()
                            .map(CategoryChore::getCategory)
                            .toList(),
                    template.getSpace()
            );

            for (CategoryChore categoryChore : matchedCategoryChores) {
                redisChoreStatsService.increment(
                        categoryChore.getCategory(),
                        template.getSpace()
                );
            }
        }


        for(ChoreInstance instance : instances){
            eventPublisher.publishEvent(ChoreInstanceCreatedEvent.create(chore.getUser().getId(),
                    instance,
                    chore.getNotificationTime(),
                    chore.getRepeatType()));
        }

        List<MissionDto.Response> userMission =
                missionService.increaseMissionCountForAction(userId,
                                UserActionType.CREATE_CHORE_WITH_SPACE)
                        .stream().filter(MissionDto.Response::isCompleted).toList();

        if(!userMission.isEmpty()){
            for (MissionDto.Response mission : userMission) {
                badgeService.evaluateBadgesMission(user);
                log.info("Mission Done with CREATE_CHORE_WITH_SPACE : {}", mission.getTitle());
            }
        }

        badgeService.evaluateBadgesOnCreate(user);

        return ChoreDto.ApiResponse.<ChoreDto.Response>builder()
                .data(ChoreDto.Response.fromEntity(saved))
                .missionResults(userMission)
                .build();
    }
}
