package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final ChoreRepository choreRepository;

    public List<ChoreResponse> getChoresBySpace(Space space){
        List<Chore> chores = choreRepository.findBySpaceAndIsDeletedFalse(space);

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
