package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.RepeatType;


public record ChoreResponse(Long choreId,
                            String title,
                            RepeatType frequency) {

    public static ChoreResponse fromEntity(Chore chore) {
        return new ChoreResponse(
                chore.getId(),
                chore.getTitle(),
                chore.getRepeatType()
        );
    }
}

