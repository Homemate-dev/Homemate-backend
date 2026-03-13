package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreStatusCountDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.notification.component.ChoreInstanceCreatedEvent;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChoreService {
    private final ChoreRepository choreRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final UserRepository userRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    private final MissionService missionService;
    private final RedisChoreStatsService redisChoreStatsService;
    private final BadgeService badgeService;

    private final ChoreInstanceGenerator choreInstanceGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public ChoreDto.Response getChore(Long userId, Long choreId) {

        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (chore.getIsDeleted()) {
            throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
        }

        return ChoreDto.Response.fromEntity(chore);
    }

    @Transactional(readOnly = true)
    public List<ChoreDto.Response> getChoreList(
            Long userId, String filter, String space,
            String repeat, Integer repeatInterval, String status) {

        if (filter == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        ChoreFilterType filterType = parseEnum(filter, ChoreFilterType.class);
        Space spaceType =
                (space != null) ? parseEnum(space, Space.class) : null;
        RepeatType repeatType =
                (repeat != null) ? parseEnum(repeat, RepeatType.class) : null;
        ChoreStatus choreStatus = (status != null) ?
                parseEnum(status, ChoreStatus.class) : null;

        Sort sort = Sort.by("startDate", "createdAt");

        List<Chore> chores = switch (filterType) {
            case ALL -> {
                if (spaceType != null) {
                    throw new CustomException(ErrorCode.VALIDATION_ERROR);
                }

                if (repeatType != null) {
                    yield choreRepository.
                            findByUserIdAndRepeatTypeAndRepeatIntervalAndIsDeletedIsFalse(
                                    userId, repeatType, repeatInterval, sort);
                } else {
                    yield choreRepository.findByUserIdAndIsDeletedIsFalse(userId, sort);
                }
            }
            case SPACE -> {
                if (spaceType == null) {
                    throw new CustomException(ErrorCode.VALIDATION_ERROR);
                }

                if (repeatType != null) {
                    yield choreRepository.
                            findByUserIdAndSpaceAndRepeatTypeAndRepeatIntervalAndIsDeletedIsFalse(
                                    userId, spaceType, repeatType, repeatInterval, sort);
                } else {
                    yield choreRepository.
                            findByUserIdAndSpaceAndIsDeletedIsFalse(userId, spaceType, sort);
                }
            }
        };

        if (choreStatus != null) {
            if (chores.isEmpty()) {
                return List.of();
            }

            List<Long> choreIds = chores.stream().map(Chore::getId).toList();

            Map<Long, ChoreStatusCountDto> countMap =
                    choreInstanceRepository.countPendingCompletedByChoreIds(choreIds)
                            .stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    ChoreStatusCountDto::choreId,
                                    dto -> dto
                            ));

            chores = chores.stream()
                    .filter(chore -> {
                        ChoreStatusCountDto c = countMap.get(chore.getId());

                        if (c == null) return false;

                        return switch (choreStatus) {
                            case PENDING -> c.pendingCount() > 0;
                            case COMPLETED -> (c.completedCount() > 0) && (c.pendingCount() == 0);
                            default -> false;
                        };
                    })
                    .toList();
        }

        return chores.stream().map(ChoreDto.Response::fromEntity).toList();
    }

    @Transactional
    public ApiResponse<ChoreDto.Response> createChores(Long userId, ChoreDto.Request request) {

        if (request.getNotificationYn() && request.getNotificationTime() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        } else if (isStartAfterEnd(request.getStartDate(),
                request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        } else if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        User userReference = userRepository.getReferenceById(userId);

        LocalDate endDate;
        if (request.getRepeatType() == RepeatType.NONE) {
            endDate = request.getStartDate();
        } else {
            endDate = request.getEndDate();
        }

        if (request.getNotificationYn()) {
            userNotificationSettingRepository.
                    enableUserNotificationSetting(userId);
        }

        RegistrationType registrationType =
                request.getRecommendYn() ? RegistrationType.RECOMMEND : RegistrationType.MANUAL;

        Chore chore = Chore.builder()
                .user(userReference)
                .title(request.getTitle())
                .notificationYn(request.getNotificationYn())
                .notificationTime(request.getNotificationTime())
                .space(request.getSpace())
                .repeatType(request.getRepeatType())
                .repeatInterval(request.getRepeatInterval())
                .startDate(request.getStartDate())
                .endDate(endDate)
                .isDeleted(false)
                .registrationType(registrationType)
                .build();

        Chore savedChore = choreRepository.save(chore);
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(
                savedChore);
        choreInstanceRepository.saveAll(instances);

        List<MissionDto.Response> userMission =
                missionService.increaseMissionCountForAction(
                                userId, UserActionType.CREATE_CHORE_MANUAL)
                        .stream().filter(MissionDto.Response::isCompleted).toList();

        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        if (!userMission.isEmpty()) {
            for (MissionDto.Response mission : userMission) {

                log.info("Mission Done with CREATE_CHORE_MANUAL - user : {}, mission : {}", userId, mission.getId());

                badgeService.evaluateBadgesMission(user);
            }
        }

        // 맞춤 알림 시간이 null일 경우 마이페이지 시간 -> 없으면 기본값(19:00) 사용
        LocalTime notificationTime = request.getNotificationTime();
        if (notificationTime == null) {
            notificationTime = userNotificationSettingRepository.findByUser(userReference)
                    .map(UserNotificationSetting::getNotificationTime)
                    .orElseGet(() -> LocalTime.of(19, 0)); // default time
        }

        // TODO: for-loop 대신 배치 처리 구현
        for (ChoreInstance instance : instances) {
            eventPublisher.publishEvent(ChoreInstanceCreatedEvent.create(userId,
                    instance,
                    notificationTime,
                    savedChore.getRepeatType()
            ));
        }

        redisChoreStatsService.increment(null, request.getSpace());
        badgeService.evaluateBadgesOnCreate(chore.getUser());

        return ApiResponse.<ChoreDto.Response>builder()
                .data(ChoreDto.Response.fromEntity(savedChore))
                .missionResults(userMission)
                .build();
    }

    @Transactional
    public ChoreDto.Response updateChores(Long userId, Long choreId, ChoreDto.Request request) {
        Chore chore = choreRepository.findByIdAndUserId(choreId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        if (request.getNotificationYn() && request.getNotificationTime() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        } else if (isStartAfterEnd(request.getStartDate(), request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 1. Chore 업데이트
        chore.setTitle(request.getTitle());
        chore.setNotificationYn(request.getNotificationYn());
        chore.setNotificationTime(request.getNotificationTime());
        chore.setSpace(request.getSpace());
        chore.setRepeatType(request.getRepeatType());
        chore.setRepeatInterval(request.getRepeatInterval());
        chore.setStartDate(request.getStartDate());

        LocalDate endDate = request.getRepeatType() == RepeatType.NONE ? request.getStartDate() : request.getEndDate();
        chore.setEndDate(endDate);

        RegistrationType registrationType = request.getRecommendYn() ? RegistrationType.RECOMMEND : RegistrationType.MANUAL;
        chore.setRegistrationType(registrationType);

        // 2. 기존 ChoreInstance 취소
        List<ChoreInstance> pendingInstances = choreInstanceRepository.findByChoreAndChoreStatus(chore, ChoreStatus.PENDING);
        pendingInstances.forEach(ChoreInstance::cancelChore);

        // 3. 새로운 ChoreInstance 생성
        List<ChoreInstance> newInstances = choreInstanceGenerator.generateInstances(chore);
        choreInstanceRepository.saveAll(newInstances);

        LocalTime notificationTime = request.getNotificationTime();
        if (notificationTime == null) {
            notificationTime = userNotificationSettingRepository.findByUser(chore.getUser())
                    .map(UserNotificationSetting::getNotificationTime)
                    .orElseGet(() -> LocalTime.of(19, 0));
        }

        for (ChoreInstance instance : newInstances) {
            eventPublisher.publishEvent(ChoreInstanceCreatedEvent.create(userId,
                    instance,
                    notificationTime,
                    chore.getRepeatType()
            ));
        }

        return ChoreDto.Response.fromEntity(chore);
    }

    @Transactional
    public void deleteChore(Long userId, Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (chore.getIsDeleted()) {
            throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
        }

        chore.softDelete();
        choreInstanceRepository.bulkSoftDeleteByChore(chore);
    }

    private boolean isStartAfterEnd(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }

    private <E extends Enum<E>> E parseEnum(String raw, Class<E> enumType) {
        try {
            return Enum.valueOf(enumType, raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
