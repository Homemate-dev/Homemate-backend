package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.dto.MissionDto.Response;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import java.util.Optional;
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

    @Transactional
    public ApiResponse<ChoreDto.Response> createChoreFromCategory(Long userId,
        Category category, Long categoryChoreId) {
        // 1. 사용자 유효성 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. CategoryChore(템플릿) 조회
        CategoryChore template = categoryChoreRepository.findById(categoryChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        // 3. ChoreRepository에서 동일한 이름과 사용자로 인해 만들어진 Chore가 있는지 검증
        if(choreRepository.existsByUserIdAndTitle(userId, template.getTitle())){
            throw new CustomException(ErrorCode.CHORE_ALREADY_REGISTERED);
        }

        // 1. 동일한 집안일 찾기 (title 기준)
        SpaceChore matchedSpaceChore = spaceChoreRepository.findByTitleKo(template.getTitle())
                .orElse(null);

        // 2. Space 결정: 매칭된 항목이 없으면 ETC
        Space space = (matchedSpaceChore != null) ? matchedSpaceChore.getSpace() : Space.ETC;

        // 3. 사용자 설정으로부터 Notification 여부, notification time의 기본 설정을 가져온다.
        UserNotificationSetting setting = userNotificationSettingRepository.findByUserId(userId)
                .orElse(UserNotificationSetting.createDefault(user, LocalTime.of(9, 0)));


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
                .build();

        // 5. 저장
        Chore saved = choreRepository.save(chore);

        // 6. 반복 인스턴스 생성
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(saved);
        choreInstanceRepository.saveAll(instances);

        // 7. Redis counting 반영
        redisChoreStatsService.increment(template.getCategory(), space);

        Optional<Response> userMission =
            missionService.increaseMissionCountForAction(userId,
                UserActionType.CREATE_CHORE_WITH_SPACE);

        return ApiResponse.<ChoreDto.Response>builder()
            .data(ChoreDto.Response.fromEntity(saved))
            .missionResults(
                userMission.map(List::of).orElseGet(List::of)
            )
            .build();
    }



}
