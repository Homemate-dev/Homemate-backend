package com.zerobase.homemate.recommend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClassifyChoreResponse(Long choreId,
                                    String title,
                                    String frequency,
                                    Space spaceName,
                                    String categoryName) {

    public static ClassifyChoreResponse fromCategory(CategoryChore categoryChore) {
        return new ClassifyChoreResponse(
                categoryChore.getId(),
                categoryChore.getTitle(),
                formatFrequency(categoryChore.getRepeatType(), categoryChore.getRepeatInterval()),
                null,
                categoryChore.getCategory().getCategoryName()

        );
    }

    private static String formatFrequency(RepeatType repeatType, Integer repeatInterval) {
        if(repeatType == null)  return "반복 없음";

        // interval이 1일 시, 매일, 매주, 매달, 매년마다로 표시
        if(repeatInterval == null || repeatInterval == 1){
            return switch(repeatType){
                case NONE -> "반복 없음";
                case DAILY -> "매일";
                case MONTHLY -> "매달";
                case WEEKLY -> "매주";
                case YEARLY -> "매년";
            };

        }

        return switch(repeatType){
            case NONE -> "반복 없음";
            case DAILY -> repeatInterval + "일";
            case MONTHLY -> repeatInterval + "달";
            case WEEKLY -> repeatInterval + "주";
            case YEARLY -> repeatInterval + "년";
        };

    }

    public static ClassifyChoreResponse fromSpace(SpaceChore spaceChore) {
        return new ClassifyChoreResponse(
                spaceChore.getId(),
                spaceChore.getTitleKo(),
                formatFrequency(spaceChore.getRepeatType(), spaceChore.getRepeatInterval()),
                spaceChore.getSpace(),
                null
        );
    }
}

