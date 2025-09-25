package com.zerobase.homemate.util;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChoreInstanceGenerator {

    private static final int MAX_INSTANCES = 1000; // 최대 인스턴스 수 제한

    public List<ChoreInstance> generateInstances(Chore chore) {
        List<ChoreInstance> instances = new ArrayList<>();

        if (chore.getRepeatType() == Chore.RepeatType.NONE) {
            instances.add(createInstance(chore, chore.getStartDate()));
        } else {
            LocalDate currentDate = chore.getStartDate();
            LocalDate endDate = chore.getEndDate();

            while (!currentDate.isAfter(endDate)) {
                instances.add(createInstance(chore, currentDate));
                currentDate = getNextDate(
                    currentDate,
                    chore.getRepeatType(),
                    chore.getRepeatInterval());
            }

            if (instances.size() >= MAX_INSTANCES) {
                throw new CustomException(ErrorCode.TOO_MANY_INSTANCES);
            }
        }

        return instances;
    }

    private ChoreInstance createInstance(Chore chore, LocalDate dueDate) {
        return ChoreInstance.builder()
                .choreId(chore.getId())
                .dueDate(dueDate)
                .choreStatus(ChoreInstance.ChoreStatus.PENDING)
                .build();
    }

    private LocalDate getNextDate(LocalDate currentDate,
        Chore.RepeatType repeatType, Integer repeatInterval) {
        return switch (repeatType) {
            case DAILY -> currentDate.plusDays(repeatInterval);
            case WEEKLY -> currentDate.plusWeeks(repeatInterval);
            case MONTHLY -> currentDate.plusMonths(repeatInterval);
            case YEARLY -> currentDate.plusYears(repeatInterval);
            default -> currentDate.plusDays(1);
        };
    }
}
