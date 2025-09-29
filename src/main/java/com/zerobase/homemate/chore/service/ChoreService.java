package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
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

    @Transactional
    public ChoreDto.Response createChores(Long userId,
        ChoreDto.CreateRequest request) {

        if (request.getNotificationYn()
            && request.getNotificationTime() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        } else if (ChoreDto.isValidDateRange(request.getStartDate(),
            request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        Chore chore = Chore.builder()
            .userId(userId)
            .title(request.getTitle())
            .notificationYn(request.getNotificationYn())
            .notificationTime(request.getNotificationTime())
            .space(request.getSpace())
            .repeatType(request.getRepeatType())
            .repeatInterval(request.getRepeatInterval())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isDeleted(false)
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
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND)) ;
        Chore chore = choreRepository.findById(choreInstance.getChoreId())
            .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        if (!chore.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        } else if (request.getNotificationYn()
            && request.getNotificationTime() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        } else if (ChoreDto.isValidDateRange(request.getStartDate(),
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

    private ChoreDto.Response updateChoreOnly(Chore chore,
        ChoreInstance choreInstance, ChoreDto.UpdateRequest request) {

        chore.setTitle(request.getTitle());
        chore.setNotificationYn(request.getNotificationYn());
        chore.setSpace(request.getSpace());

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

        // TODO : 벌크 업데이트 JPQL 사용 전환
        futureInstances.forEach(instance -> {
                instance.setTitleSnapshot(request.getTitle());
                instance.setNotificationTime(
                    request.getNotificationYn() ? request.getNotificationTime() : null);
            }
        );

        choreInstanceRepository.saveAll(futureInstances);
        Chore updatedChore = choreRepository.save(chore);

        return ChoreDto.Response.fromEntity(updatedChore);
    }

    private ChoreDto.Response updateChoreInstance(Chore chore,
        ChoreInstance choreInstance, ChoreDto.UpdateRequest request) {

        Chore newChore = Chore.builder()
            .userId(chore.getUserId())
            .title(request.getTitle())
            .notificationYn(request.getNotificationYn())
            .notificationTime(request.getNotificationTime())
            .space(request.getSpace())
            .repeatType(request.getRepeatType())
            .repeatInterval(request.getRepeatInterval())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isDeleted(false)
            .build();

        Chore savedNewChore = choreRepository.save(newChore);
        choreRepository.flush();

        // TODO : 벌크 업데이트 JPQL 사용 전환
        if (request.getApplyToFuture()) {
            List<ChoreInstance> futureInstances = choreInstanceRepository
                .findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
                    chore.getId(),
                    choreInstance.getDueDate(),
                    ChoreStatus.PENDING
                );
            futureInstances.forEach(instance -> {
               instance.setChoreStatus(ChoreStatus.CANCELLED);
            });
            choreInstanceRepository.saveAll(futureInstances);
            choreInstanceRepository.flush();
        } else {
            choreInstance.setChoreStatus(ChoreStatus.CANCELLED);
            choreInstanceRepository.save(choreInstance);
            choreInstanceRepository.flush();
        }

        List<ChoreInstance> newInstances =
            choreInstanceGenerator.generateInstances(savedNewChore);
        choreInstanceRepository.saveAll(newInstances);

        return ChoreDto.Response.fromEntity(savedNewChore);
    }
}
