package com.zerobase.homemate.recommend.dto;

import com.zerobase.homemate.entity.enums.Space;

public interface SpaceChoreResponse {
    Long getId();
    String getTitleKo();
    Space getSpace();
}
