package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Chore;


public record ChoreResponse(Long choreId,
                            String title,
                            Chore.RepeatType frequency) {

    public static ChoreResponse fromEntity(Chore chore) {
        return new ChoreResponse(
                chore.getId(),
                chore.getTitle(),
                chore.getRepeatType()
        );
    }
}

