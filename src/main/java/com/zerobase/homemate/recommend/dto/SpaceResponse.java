package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.Space;

public record SpaceResponse(Long id,
                            String code,
                            String title,
                            Boolean isActive) {

    public static SpaceResponse fromEntity(Space space) {
        return new SpaceResponse(
                space.getId(),
                space.getCode(),
                space.getNameKo(),
                space.getIsActive());
    }
}
