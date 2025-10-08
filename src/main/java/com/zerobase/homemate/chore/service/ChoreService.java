package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public ChoreDto.Response createChores(Long userId,
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

        Chore chore = Chore.builder()
            .title(request.getTitle())
            .notificationYn(request.getNotificationYn())
            .notificationTime(request.getNotificationTime())
            .space(request.getSpace())
            .repeatType(request.getRepeatType())
            .repeatInterval(request.getRepeatInterval())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isDeleted(false)
            .user(userReference)
            .build();

        Chore savedChore = choreRepository.save(chore);
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(
            savedChore);
        choreInstanceRepository.saveAll(instances);

        return ChoreDto.Response.fromEntity(savedChore);
    }

    @Transactional
    public ChoreDto.Response updateChores(Long userId, Long choreInstanceId,
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

    private ChoreDto.Response updateChoreInstance(Chore chore,
        ChoreInstance choreInstance, ChoreDto.UpdateRequest request) {

        if (request.getApplyToAll()) {
            List<ChoreInstance> futureInstances = choreInstanceRepository
                .findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
                    chore.getId(),
                    choreInstance.getDueDate(),
                    ChoreStatus.PENDING
                );
            futureInstances.forEach(instance ->
                instance.setChoreStatus(ChoreStatus.CANCELLED));
        } else {
            choreInstance.setChoreStatus(ChoreStatus.CANCELLED);
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

    private ChoreDto.Response updateChoreOnly(Chore chore,
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

        return ChoreDto.Response.fromEntity(chore);
    }

    private boolean isStartAfterEnd(LocalDate startDate, LocalDate endDate) {
    @Transactional
    public ChoreInstanceDto.Response completeChore(Long userId,
        Long choreInstanceId) {
        ChoreInstance choreInstance =
            choreInstanceRepository.findById(choreInstanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_INSTANCE_NOT_FOUND));
        Chore chore = choreInstance.getChore();

        if (!chore.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        } else if (choreInstance.getChoreStatus() == ChoreStatus.PENDING) {
            choreInstance.completeChore();
        } else if (choreInstance.getChoreStatus() == ChoreStatus.COMPLETED) {
            choreInstance.cancelCompleteChore();
        } else if (choreInstance.getChoreStatus() == ChoreStatus.CANCELLED
            || choreInstance.getChoreStatus() == ChoreStatus.DELETED) {
            throw new CustomException(ErrorCode.CHORE_ALREADY_DELETED);
        }

        return ChoreInstanceDto.Response.fromEntity(choreInstance);
    }

    private boolean isStartAfterEnd(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }
}
