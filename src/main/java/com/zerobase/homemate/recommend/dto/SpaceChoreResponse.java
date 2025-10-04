package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;

public record SpaceChoreResponse (Long spaceId,
                                  String spaceTitle,
                                  Long choreId,
                                  String choreTitle,
                                  RepeatType defaultFreq) {

    public static SpaceChoreResponse fromEntity(SpaceChore spaceChore){
        return new SpaceChoreResponse(
                spaceChore.getSpace().getId(),
                spaceChore.getSpace().getNameKo(),
                spaceChore.getChore().getId(),
                spaceChore.getChore().getTitle(),
                spaceChore.getChore().getRepeatType()
        );
    }
}
