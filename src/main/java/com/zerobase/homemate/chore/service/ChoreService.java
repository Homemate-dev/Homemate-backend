package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.badge.service.UserBadgeStatsService;
import com.zerobase.homemate.chore.dto.ChoreCounts;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.notification.component.ChoreInstanceCreatedEvent;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final ChoreInstanceGenerator choreInstanceGenerator;
    private final UserRepository userRepository;
    private final MissionService missionService;
    private final ApplicationEventPublisher eventPublisher;

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final RedisChoreStatsService redisChoreStatsService;
    private final BadgeService badgeService;
    private final UserBadgeStatsService userBadgeStatsService;

    @Transactional
    public ApiResponse<ChoreDto.Response> createChores(Long userId,
        ChoreDto.CreateRequest request) {

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
            .build();

        Chore savedChore = choreRepository.save(chore);
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(
            savedChore);
        choreInstanceRepository.saveAll(instances);

        List<MissionDto.Response> userMission =
            missionService.increaseMissionCountForAction(
            userId, UserActionType.CREATE_CHORE_MANUAL)
                .stream().filter(MissionDto.Response::isCompleted).toList();

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

        return ApiResponse.<ChoreDto.Response>builder()
            .data(ChoreDto.Response.fromEntity(savedChore))
            .missionResults(userMission)
            .build();
    }

    @Transactional
    public ApiResponse<ChoreDto.Response> updateChores(Long userId, Long choreInstanceId,
        ChoreDto.UpdateRequest request) {

        ChoreInstance choreInstance =
            choreInstanceRepository.findById(choreInstanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));
        Chore chore = choreInstance.getChore();

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        } else if (choreInstance.getChoreStatus() != ChoreStatus.PENDING) {
            throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
        } else if (request.getNotificationYn()
            && request.getNotificationTime() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        } else if (isStartAfterEnd(request.getStartDate(),
            request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        boolean isRepeatChanged =
            !Objects.equals(chore.getRepeatType(), request.getRepeatType()) ||
                !Objects.equals(chore.getRepeatInterval(),
                    request.getRepeatInterval());
        boolean startDateChanged =
            !chore.getStartDate().equals(request.getStartDate());
        boolean endDateChanged =
            !chore.getEndDate().equals(request.getEndDate());

        if (isRepeatChanged || startDateChanged || endDateChanged) {
            return updateChoreInstance(chore, choreInstance, request);
        } else {
            return updateChoreOnly(chore, choreInstance, request);
        }
    }

    private ApiResponse<ChoreDto.Response> updateChoreInstance(Chore chore,
        ChoreInstance choreInstance, ChoreDto.UpdateRequest request) {

        if (request.getApplyToAfter()) {
            List<ChoreInstance> futureInstances = choreInstanceRepository
                .findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
                    chore.getId(),
                    choreInstance.getDueDate(),
                    ChoreStatus.PENDING
                );
            futureInstances.forEach(ChoreInstance::cancelChore);
        } else {
            choreInstance.cancelChore();
        }

        return createChores(chore.getUser().getId(),
            ChoreDto.CreateRequest.builder()
            .title(request.getTitle())
            .notificationYn(request.getNotificationYn())
            .notificationTime(request.getNotificationTime())
            .space(request.getSpace())
            .repeatType(request.getRepeatType())
            .repeatInterval(request.getRepeatInterval())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build());
    }

    private ApiResponse<ChoreDto.Response> updateChoreOnly(Chore chore,
        ChoreInstance choreInstance, ChoreDto.UpdateRequest request) {

        if (!request.getNotificationYn()) {
            chore.setNotificationTime(null);
        } else {
            chore.setNotificationTime(request.getNotificationTime());
        }

        List<ChoreInstance> futureInstances = choreInstanceRepository
            .findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
                chore.getId(),
                choreInstance.getDueDate(),
                ChoreStatus.PENDING
            );

        futureInstances.forEach(instance -> {
                instance.setTitleSnapshot(request.getTitle());
                instance.setNotificationTime(
                    request.getNotificationYn() ? request.getNotificationTime() : null);
            }
        );

        chore.setTitle(request.getTitle());
        chore.setNotificationYn(request.getNotificationYn());
        chore.setSpace(request.getSpace());

        return ApiResponse.<ChoreDto.Response>builder()
            .data(ChoreDto.Response.fromEntity(chore))
            .build();
    }

    @Transactional
    public ApiResponse<ChoreInstanceDto.Response> completeChore(Long userId,
        Long choreInstanceId) {
        ChoreInstance choreInstance =
            choreInstanceRepository.findById(choreInstanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));
        Chore chore = choreInstance.getChore();

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        final List<MissionDto.Response> userMission;

        switch (choreInstance.getChoreStatus()) {
            case PENDING -> {
                choreInstance.completeChore();
                userMission =
                    missionService.applyChoreCompletionByStatus(userId,
                    choreInstance, true);
            }
            case COMPLETED -> {
                choreInstance.cancelCompleteChore();
                userMission =
                    missionService.applyChoreCompletionByStatus(userId,
                    choreInstance, false);
            }
            case CANCELLED, DELETED -> throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
            default -> throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        userBadgeStatsService.incrementCount(userId);
        if(chore.getSpace() != null) {
            userBadgeStatsService.incrementSpaceCount(userId, chore.getSpace());
        }

        boolean isTitleBadgeTarget = Arrays.stream(BadgeType.values())
                        .anyMatch(type -> chore.getTitle().equalsIgnoreCase(type.name()));

        if(isTitleBadgeTarget) {
            userBadgeStatsService.incrementTitleCount(userId, chore.getTitle());
        }

        badgeService.evaluateBadges(chore.getUser());
      
        return ApiResponse.<ChoreInstanceDto.Response>builder()
            .data(ChoreInstanceDto.Response.fromEntity(choreInstance))
            .missionResults(userMission)
            .build();
    }

    private boolean isStartAfterEnd(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }

    public ChoreDto.Response getChore(Long userId, Long choreInstanceId) {

        ChoreInstance choreInstance =
            choreInstanceRepository.findById(choreInstanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));
        Chore chore = choreInstance.getChore();

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (chore.getIsDeleted()) {
            throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
        } else if (choreInstance.getChoreStatus() == ChoreStatus.CANCELLED ||
        choreInstance.getChoreStatus() == ChoreStatus.DELETED) {
            throw new CustomException(ErrorCode.CHORE_INSTANCE_ALREADY_DELETED);
        }

        return ChoreDto.Response.fromEntity(chore);
    }

    public List<ChoreInstanceDto.Response> getChoreInstancesByDate(Long userId,
        LocalDate date) {

        EnumSet<ChoreStatus> includedStatuses =
            EnumSet.of(ChoreStatus.PENDING, ChoreStatus.COMPLETED);

        List<ChoreInstance> choreInstances =
            choreInstanceRepository
                .findAllByChore_User_IdAndDueDateAndChoreStatusInOrderByNotificationTimeAscIdAsc(
                    userId, date, includedStatuses);

        if (choreInstances.isEmpty()) {
            return List.of();
        } else {
            return choreInstances.stream().map(ChoreInstanceDto.Response::fromEntity).toList();
        }
    }

    public List<LocalDate> getCalendarMarkedDates(Long userId,
        LocalDate startDate, LocalDate endDate) {

        if (isStartAfterEnd(startDate, endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        EnumSet<ChoreStatus> includedStatuses =
            EnumSet.of(ChoreStatus.PENDING, ChoreStatus.COMPLETED);

        List<LocalDate> dates = choreInstanceRepository.findDatesHavingInstances(userId,
            startDate, endDate, includedStatuses);

        if (dates.isEmpty()) {
            return List.of();
        } else {
            return dates;
        }
    }

    @Transactional
    public void deleteChore(Long userId, Long choreInstanceId,
        boolean applyToAfter) {

        ChoreInstance choreInstance =
            choreInstanceRepository.findById(choreInstanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));
        Chore chore = choreInstance.getChore();

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (choreInstance.getChoreStatus() == ChoreStatus.PENDING ||
            choreInstance.getChoreStatus() == ChoreStatus.COMPLETED) {
            if (chore.getRepeatType() == RepeatType.NONE) {
                choreInstance.softDelete();
                chore.softDelete();
            } else {
                if (choreInstance.getDueDate().equals(chore.getStartDate())) {
                    LocalDate nextDate = choreInstanceGenerator.getNextDate(
                        choreInstance.getDueDate(),
                        chore.getRepeatType(),
                        chore.getRepeatInterval());

                    while(choreInstanceRepository.existsByChoreAndDueDate(chore, nextDate)) {
                        nextDate = choreInstanceGenerator.getNextDate(
                            nextDate,
                            chore.getRepeatType(),
                            chore.getRepeatInterval());
                    }

                    chore.setStartDate(nextDate);
                } else if (choreInstance.getDueDate().equals(chore.getEndDate())) {
                    LocalDate beforeDate = choreInstanceGenerator.getBeforeDate(
                        choreInstance.getDueDate(),
                        chore.getRepeatType(),
                        chore.getRepeatInterval());

                    while(choreInstanceRepository.existsByChoreAndDueDate(chore, beforeDate)) {
                        beforeDate = choreInstanceGenerator.getNextDate(
                            beforeDate,
                            chore.getRepeatType(),
                            chore.getRepeatInterval());
                    }

                    chore.setEndDate(beforeDate);
                }

                if (applyToAfter) {
                    choreInstanceRepository.bulkSoftDeleteAfterByChoreAndStatuses(
                        chore, choreInstance.getDueDate(),
                        ChoreStatus.DELETED, LocalDateTime.now());
                } else {
                    choreInstance.softDelete();
                }
            }

            softDeleteChoreIfAllInstancesDeleted(chore);
        } else {
            throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
        }
    }

    private void softDeleteChoreIfAllInstancesDeleted(Chore chore) {
        List<ChoreInstance> activeInstances =
            choreInstanceRepository.findByChoreAndChoreStatus(
                chore, ChoreStatus.PENDING);

        if (activeInstances.isEmpty()) {
            Chore refChore = choreRepository.getReferenceById(chore.getId());
            refChore.softDelete();
        }
    }

    public double getTodayCompleteRate(Long userId) {
        EnumSet<ChoreStatus> includedStatuses =
            EnumSet.of(ChoreStatus.PENDING, ChoreStatus.COMPLETED);

        ChoreCounts counts =
            choreInstanceRepository.countTodayTotalsAndCompleted(
                userId, LocalDate.now(), includedStatuses);

        long total = counts.total();
        if (total == 0) {
            return 0.0;
        }

        long completed = counts.completed();
        double rate = (double) completed / total * 100;

        return Math.round(rate * 100.0) / 100.0;
    }
}
