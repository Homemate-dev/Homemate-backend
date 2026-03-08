package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.badge.service.BadgeService;
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
    public ChoreDto.ApiResponse<ChoreInstanceDto.Response> patchCompletionStatus(Long userId, Long choreInstanceId) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

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
    public void deleteChoreInstance(Long userId, Long choreInstanceId) {
        ChoreInstance choreInstance = choreInstanceRepository.findByIdAndChore_User_Id(choreInstanceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));

        if (choreInstance.getChoreStatus() == ChoreStatus.DELETED) {
            throw new CustomException(ErrorCode.CHORE_INSTANCE_ALREADY_DELETED);
        }

        choreInstance.softDelete();
    }
}
