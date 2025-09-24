package com.zerobase.homemate.util;

import com.zerobase.homemate.entity.Chores;
import com.zerobase.homemate.entity.ChoreInstances;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChoreInstanceGenerator {

    private static final int MAX_INSTANCES = 1000; // 최대 인스턴스 수 제한

    public List<ChoreInstances> generateInstances(Chores chores) {
        try {

            validateChoreData(chores);
            List<ChoreInstances> instances = new ArrayList<>();
            
            if (chores.getRepeatType() == Chores.RepeatType.NONE) {
                instances.add(createInstance(chores, chores.getStartDate()));
            } else {
                LocalDate currentDate = chores.getStartDate();
                LocalDate endDate = chores.getEndDate() != null ? chores.getEndDate() : chores.getStartDate().plusYears(1);
                
                int instanceCount = 0;
                while (!currentDate.isAfter(endDate) && instanceCount < MAX_INSTANCES) {
                    instances.add(createInstance(chores, currentDate));
                    currentDate = getNextDate(currentDate, chores.getRepeatType(), chores.getRepeatInterval());
                    instanceCount++;
                }

                if (instanceCount >= MAX_INSTANCES) {
                    throw new CustomException(ErrorCode.TOO_MANY_INSTANCES);
                }
            }
            
            return instances;
            
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT, "날짜 형식이 올바르지 않습니다: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인스턴스 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private void validateChoreData(Chores chores) {
        if (chores.getStartDate() == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "시작일은 필수입니다.");
        }

        if (chores.getRepeatType() != Chores.RepeatType.NONE && chores.getEndDate() != null) {
            if (chores.getEndDate().isBefore(chores.getStartDate())) {
                throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
            }
        }

        if (chores.getRepeatType() != Chores.RepeatType.NONE) {
            if (chores.getRepeatInterval() != null && chores.getRepeatInterval() <= 0) {
                throw new CustomException(ErrorCode.INVALID_REPEAT_INTERVAL);
            }
        }

        if (chores.getNotificationYn() && chores.getNotificationTime() != null) {
            validateNotificationTime(
                String.valueOf(chores.getNotificationTime()));
        }
    }
    
    private void validateNotificationTime(String notificationTime) {
        try {
            String[] timeParts = notificationTime.split(":");
            if (timeParts.length != 2) {
                throw new CustomException(ErrorCode.INVALID_NOTIFICATION_TIME);
            }
            
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new CustomException(ErrorCode.INVALID_NOTIFICATION_TIME);
            }
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_NOTIFICATION_TIME);
        }
    }
    
    private ChoreInstances createInstance(Chores chores, LocalDate dueDate) {
        return ChoreInstances.builder()
                .choreId(chores.getId())
                .dueDate(dueDate)
                .status(ChoreInstances.Status.PENDING)
                .build();
    }
    
    private LocalDate getNextDate(LocalDate currentDate, Chores.RepeatType repeatType, Integer repeatInterval) {
        if (repeatInterval == null || repeatInterval <= 0) {
            repeatInterval = 1;
        }
        
        return switch (repeatType) {
            case DAILY -> currentDate.plusDays(repeatInterval);
            case WEEKLY -> currentDate.plusWeeks(repeatInterval);
            case MONTHLY -> currentDate.plusMonths(repeatInterval);
            case YEARLY -> currentDate.plusYears(repeatInterval);
            default -> currentDate.plusDays(1);
        };
    }
}
