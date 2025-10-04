package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.SpaceChore;

public record SpaceChoreResponse (Long spaceId,
                                  String spaceTitle,
                                  Long choreId,
                                  String choreTitle,
                                  String frequency){

    public static SpaceChoreResponse fromEntity(SpaceChore spaceChore){
        return new SpaceChoreResponse(
                spaceChore.getSpace().getId(),
                spaceChore.getSpace().getNameKo(),
                spaceChore.getChore().getId(),
                spaceChore.getChore().getTitle(),
                spaceChore.getDefaultFreq().name()
        );
    }
}
