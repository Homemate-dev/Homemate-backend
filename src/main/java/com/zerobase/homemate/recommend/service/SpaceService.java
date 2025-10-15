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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceChoreRepository spaceChoreRepository;
    private final int DEFAULT_LIMIT = 5;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.NONE, 4
    );

    public List<ClassifyChoreResponse> getChoresBySpace(Space space, int page){
        if (space == null) {
            throw new CustomException(ErrorCode.SPACE_NOT_FOUND);
        }

        if (page < 0 || page > 2) {
            throw new CustomException(ErrorCode.UNVALID_PAGE);
        }

        Pageable pageable = PageRequest.of(page, DEFAULT_LIMIT);

        List<SpaceChore> randomChores = spaceChoreRepository.findBySpace(space, pageable);

        if (randomChores.isEmpty()) {
            throw new CustomException(ErrorCode.CHORE_NOT_FOUND);
        }

        // 페이지 내에서 RepeatType 우선순위 정렬
        List<SpaceChore> pageSpaceChores = randomChores.stream()
                .sorted(Comparator.comparingInt(c -> REPEAT_PRIORITY.get(c.getRepeatType())))
                .toList();

        return pageSpaceChores.stream()
                .map(ClassifyChoreResponse::fromSpace)
                .toList();
    }

    public List<SpaceResponse> getAllSpaces() {
        return Arrays.stream(Space.values())
                .map(space -> new SpaceResponse(space.name(), space))
                .toList();
    }
}
