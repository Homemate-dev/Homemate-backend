package com.zerobase.homemate.util;

import com.zerobase.homemate.entity.Chores;
import com.zerobase.homemate.entity.ChoreInstances;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChoreInstanceGenerator {

    private static final int MAX_INSTANCES = 1000; // 최대 인스턴스 수 제한

    public List<ChoreInstances> generateInstances(Chores chores) {
        List<ChoreInstances> instances = new ArrayList<>();

        if (chores.getRepeatType() == Chores.RepeatType.NONE) {
            instances.add(createInstance(chores, chores.getStartDate()));
        } else {
            LocalDate currentDate = chores.getStartDate();
            LocalDate endDate = chores.getEndDate();

            int instanceCount = 0;
            while (!currentDate.isAfter(endDate)) {
                instances.add(createInstance(chores, currentDate));
                currentDate = getNextDate(
                    currentDate,
                    chores.getRepeatType(),
                    chores.getRepeatInterval());
                instanceCount++;
            }

            if (instanceCount >= MAX_INSTANCES) {
                throw new CustomException(ErrorCode.TOO_MANY_INSTANCES);
            }
        }

        return instances;
    }

    private ChoreInstances createInstance(Chores chores, LocalDate dueDate) {
        return ChoreInstances.builder()
                .choreId(chores.getId())
                .dueDate(dueDate)
                .status(ChoreInstances.Status.PENDING)
                .build();
    }

    private LocalDate getNextDate(LocalDate currentDate,
        Chores.RepeatType repeatType, Integer repeatInterval) {
        return switch (repeatType) {
            case DAILY -> currentDate.plusDays(repeatInterval);
            case WEEKLY -> currentDate.plusWeeks(repeatInterval);
            case MONTHLY -> currentDate.plusMonths(repeatInterval);
            case YEARLY -> currentDate.plusYears(repeatInterval);
            default -> currentDate.plusDays(1);
        };
    }
}
