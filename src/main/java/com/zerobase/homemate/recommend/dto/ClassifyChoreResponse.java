package com.zerobase.homemate.recommend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClassifyChoreResponse(Long choreId,
                                    String title,
                                    String frequency,
                                    Space spaceName,
                                    String categoryName,
                                    Boolean isDuplicate) {

    public static ClassifyChoreResponse fromCategory(CategoryChore categoryChore,
                                                     boolean isDuplicate) {
        String categoryName;

        switch (categoryChore.getCategoryType()) {
            case SEASON -> categoryName =
                    categoryChore.getSeason().name();

            case MONTHLY -> categoryName =
                    categoryChore.getCategories().getTitle();

            default -> categoryName =
                    categoryChore.getCategory().getCategoryName();
        }

        return new ClassifyChoreResponse(
                categoryChore.getId(),
                categoryChore.getTitle(),
                formatFrequency(
                        categoryChore.getRepeatType(),
                        categoryChore.getRepeatInterval()
                ),
                null,
                categoryName,
                isDuplicate
        );
    }

    private static String formatFrequency(RepeatType repeatType, Integer repeatInterval) {
        if(repeatType == null)  return "한번";

        int interval = (repeatInterval == null) ? 0 : repeatInterval;

        String frequencyKey = repeatType.name() + "_" + interval;

        return switch(frequencyKey){
            case "NONE_0" -> "한번";
            case "DAILY_1" -> "매일";
            case "WEEKLY_1" -> "1주";
            case "WEEKLY_2" -> "2주";
            case "MONTHLY_1" -> "매달";
            case "MONTHLY_3" -> "3개월";
            case "MONTHLY_6" -> "6개월";
            case "YEARLY_1" -> "매년";

            default -> throw new CustomException(ErrorCode.INVALID_FREQUENCY);
        };

    }

    public static ClassifyChoreResponse fromSpace(SpaceChore spaceChore, boolean isDuplicate) {
        return new ClassifyChoreResponse(
                spaceChore.getId(),
                spaceChore.getTitleKo(),
                formatFrequency(spaceChore.getRepeatType(), spaceChore.getRepeatInterval()),
                spaceChore.getSpace(),
                null,
                isDuplicate
        );
    }
}

