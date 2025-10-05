package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;

public record SpaceChoreResponse (Long choreId,
                                  String choreTitle,
                                  RepeatType frequency) {

    public static SpaceChoreResponse fromEntity(SpaceChore spaceChore){
        return new SpaceChoreResponse(
                spaceChore.getChore().getId(),
                spaceChore.getChore().getTitle(),
                spaceChore.getChore().getRepeatType()
        );
    }
}
