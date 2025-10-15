package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;


public record ClassifyChoreResponse(Long choreId,
                                    String title,
                                    String frequency,
                                    Space spaceName) {

//    public static ClassifyChoreResponse fromEntity(CategoryChore chore) {
//        return new ClassifyChoreResponse(
//                chore.getId(),
//                chore.getTitle(),
//                formatFrequency(chore.getRepeatType(), chore.getRepeatInterval())
//        );
//    }

    private static String formatFrequency(RepeatType repeatType, Integer repeatInterval) {
        if(repeatType == null)  return "반복 없음";

        // interval이 1일 시, 매일, 매주, 매달, 매년마다로 표시
        if(repeatInterval == null || repeatInterval == 1){
            return switch(repeatType){
                case NONE -> null;
                case DAILY -> "매일";
                case MONTHLY -> "매달";
                case WEEKLY -> "매주";
                case YEARLY -> "매년";
            };

        }

        return switch(repeatType){
            case NONE -> null;
            case DAILY -> repeatInterval + "일";
            case MONTHLY -> repeatInterval + "주";
            case WEEKLY -> repeatInterval + "달";
            case YEARLY -> repeatInterval + "년";
        };

    }

    public static ClassifyChoreResponse fromSpace(SpaceChore spaceChore) {
        return new ClassifyChoreResponse(
                spaceChore.getId(),
                spaceChore.getTitleKo(),
                formatFrequency(spaceChore.getRepeatType(), spaceChore.getRepeatInterval()),
                spaceChore.getSpace()
        );
    }
}

