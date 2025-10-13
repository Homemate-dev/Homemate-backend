package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final ChoreRepository choreRepository;
    private final SpaceChoreRepository spaceChoreRepository;

    public List<ChoreResponse> getChoresBySpace(Space space){

        // Enum으로부터 SpaceChore Entity를 조회한다.
        SpaceChore spaceChore = spaceChoreRepository.findBySpace(space).orElseThrow(
                () -> new CustomException(ErrorCode.SPACE_NOT_FOUND)
        );

        // SpaceChore 기준으로 Chore를 조회한다.
        List<Chore> chores = choreRepository.findBySpaceChoreAndIsDeletedFalse(spaceChore);

        // 응답을 최대 4개까지 반환한다.
        return chores.stream()
                .map(ChoreResponse::fromEntity)
                .limit(4)
                .toList();
    }

    public List<Map<String, String>> getAllSpaces() {
        return Arrays.stream(Space.values())
                .map(space -> Map.of(
                        "name", space.name(),
                        "description", space.getDescription()
                ))
                .toList();
    }
}
