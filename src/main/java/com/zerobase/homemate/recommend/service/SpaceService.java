package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceChoreRepository spaceChoreRepository;
    private final int DEFAULT_LIMIT = 4;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.NONE, 4
    );

    public List<ClassifyChoreResponse> getChoresBySpace(Space space){
        if(space == null){
            throw new CustomException(ErrorCode.SPACE_NOT_FOUND);
        }

        List<SpaceChore> randomChores = spaceChoreRepository.findBySpace(
                space,
                Pageable.ofSize(DEFAULT_LIMIT)
        );

        if(randomChores.isEmpty()){
            throw new CustomException(ErrorCode.CHORE_NOT_FOUND);
        }

        return randomChores.stream()
                .sorted(Comparator.comparingInt(spaceChore -> REPEAT_PRIORITY.get(spaceChore.getRepeatType())))
                .map(ClassifyChoreResponse::fromSpace)
                .toList();
    }

    public List<SpaceResponse> getAllSpaces() {
        return Arrays.stream(Space.values())
                .map(space -> new SpaceResponse(space.name(), space))
                .toList();
    }
}
