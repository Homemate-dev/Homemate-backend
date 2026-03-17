package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.chore.dto.ChoreCompletionRateResponse;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.mission.service.MissionService;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChoreInstanceService {

    private final ChoreInstanceRepository choreInstanceRepository;
    private final MissionService missionService;
    private final BadgeService badgeService;

    @Transactional(readOnly = true)
    public ChoreInstanceDto.Response getChoreInstance(Long userId, Long choreInstanceId) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

        return ChoreInstanceDto.Response.fromEntity(choreInstance);
    }

    @Transactional(readOnly = true)
    public List<ChoreInstanceDto.Response> getChoreInstancesByDate(Long userId, LocalDate date) {
        return choreInstanceRepository.findChoreInstancesByDate(userId, date)
                .stream()
                .map(ChoreInstanceDto.Response::fromEntity)
                .toList();
    }

    @Transactional
    public ChoreDto.ApiResponse<ChoreInstanceDto.Response> completeInstance(Long userId, Long choreInstanceId) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

        if (choreInstance.getChoreStatus() != ChoreStatus.PENDING) {
            throw new CustomException(ErrorCode.CHORE_INSTANCE_STATUS_CONFLICT);
        }

        choreInstance.completeChore();
        List<MissionDto.Response> missionResponse = missionService.applyChoreCompletionByStatus(userId, choreInstance, false);

        Chore chore = choreInstance.getChore();
        User user = chore.getUser();
        badgeService.evaluateBadges(user, chore);
        badgeService.evaluateBadgesOnCompletion(user, choreInstance);

        return ChoreDto.ApiResponse.<ChoreInstanceDto.Response>builder()
                .data(ChoreInstanceDto.Response.fromEntity(choreInstance))
                .missionResults(missionResponse)
                .build();
    }

    @Transactional
    public ChoreInstanceDto.Response undoInstanceCompletion(Long userId, Long choreInstanceId) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

        if (choreInstance.getChoreStatus() != ChoreStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CHORE_INSTANCE_STATUS_CONFLICT);
        }

        choreInstance.cancelChoreCompletion();

        return ChoreInstanceDto.Response.fromEntity(choreInstance);
    }

    @Transactional
    public ChoreInstanceDto.Response updateChoreInstance(Long userId, Long choreInstanceId, ChoreInstanceDto.Request request) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

        choreInstance.updateByDto(request);

        return ChoreInstanceDto.Response.fromEntity(choreInstance);
    }

    @Transactional
    public void deleteChoreInstance(Long userId, Long choreInstanceId) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

        if (choreInstance.getChoreStatus() == ChoreStatus.DELETED) {
            throw new CustomException(ErrorCode.CHORE_INSTANCE_ALREADY_DELETED);
        }

        choreInstance.softDelete();
    }

    @Transactional(readOnly = true)
    public ChoreCompletionRateResponse getChoreCompletionRate(Long userId, LocalDate date) {
        List<ChoreInstance> choreInstanceList = choreInstanceRepository.findChoreInstancesByDate(userId, date);

        if (choreInstanceList.isEmpty()) {
            return new ChoreCompletionRateResponse(0.0);
        }

        long totalCount = choreInstanceList.size();
        long completedCount = choreInstanceList.stream()
                .filter(ci -> ci.getChoreStatus() == ChoreStatus.COMPLETED)
                .count();

        double rate = (double) completedCount / totalCount * 100;
        double roundedRate = Math.round(rate * 100.0) / 100.0;

        return new ChoreCompletionRateResponse(roundedRate);
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getCalendarMarkedDates(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        return choreInstanceRepository.findDatesHavingInstances(
                userId,
                startDate,
                endDate,
                EnumSet.of(ChoreStatus.PENDING, ChoreStatus.COMPLETED)
        );
    }

}
