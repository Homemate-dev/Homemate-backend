package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import java.time.LocalDate;
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
        } else if (isInValidDateRange(request.getStartDate(),
            request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        User userReference = userRepository.getReferenceById(userId);

        Chore chore = Chore.builder()
            .user(userReference)
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

    private boolean isInValidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }
}
