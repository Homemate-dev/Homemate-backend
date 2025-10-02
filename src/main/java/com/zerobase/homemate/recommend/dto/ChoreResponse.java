package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Chore;


public record ChoreResponse(Long choreId,
                            String title,
                            String frequency) {

    public static ChoreResponse fromEntity(Chore chore) {
        return new ChoreResponse(
                chore.getId(),
                chore.getTitle(),
                chore.getRepeatType().name()
        );
    }
}

