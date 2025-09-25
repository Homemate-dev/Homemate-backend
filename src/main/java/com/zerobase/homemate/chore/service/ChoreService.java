package com.zerobase.homemate.chore.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
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
}
