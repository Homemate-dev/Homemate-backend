package com.zerobase.homemate.chores.service;

import com.zerobase.homemate.chores.dto.ChoresDto;
import com.zerobase.homemate.entity.Chores;
import com.zerobase.homemate.entity.ChoreInstances;
import com.zerobase.homemate.repository.ChoresRepository;
import com.zerobase.homemate.repository.ChoreInstancesRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChoresService {

    private final ChoresRepository choresRepository;
    private final ChoreInstancesRepository choreInstancesRepository;
    private final ChoreInstanceGenerator choreInstanceGenerator;

    @Transactional
    public ChoresDto.Response createChores(Long userId,
        ChoresDto.CreateRequest request) {

        try {
            Chores chores = Chores.builder()
                    .userId(userId)
                    .title(request.getTitle())
                    .notificationYn(request.getNotificationYn())
                    .notificationTime(
                        LocalTime.parse(request.getNotificationTime()))
                    .space(request.getSpace())
                    .repeatType(request.getRepeatType())
                    .repeatInterval(request.getRepeatInterval())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .isDeleted(false)
                    .build();

            Chores savedChores = choresRepository.save(chores);
            List<ChoreInstances> instances = choreInstanceGenerator.generateInstances(savedChores);
            choreInstancesRepository.saveAll(instances);

          return ChoresDto.Response.fromEntity(savedChores);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
