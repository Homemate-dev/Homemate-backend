package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceChoreRepository spaceChoreRepository;
    private final ChoreRepository choreRepository;

    private final Random random = new Random();

    // 1. 모든 공간 조회
    public List<SpaceResponse> getAllSpaces() {
        return spaceRepository.findAll().stream()
                .map(SpaceResponse::fromEntity)
                .toList();
    }

    // 공간별 집안일 전체 조회
    public List<SpaceChore> getAllChoresBySpace(Long spaceId){
        Space space = spaceRepository.findByIdAndIsActiveTrue(spaceId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPACE_NOT_FOUND));

        return spaceChoreRepository.findBySpaceAndIsActiveTrue(space);
    }

    // 랜덤 추천(최대 4개)
    public List<SpaceChoreResponse> getRandomChoresBySpace(Long spaceId){
        List<SpaceChore> spaceChores = spaceChoreRepository.findBySpaceAndIsActiveTrue(
                spaceRepository.findByIdAndIsActiveTrue(spaceId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SPACE_NOT_FOUND))
        );

        Collections.shuffle(spaceChores, random);

        return spaceChores.stream()
                .limit(4)
                .map(SpaceChoreResponse::fromEntity)
                .toList();
    }

    /**
     * SpaceChore 기준으로 사용자가 실제 수행할 Chore 인스턴스 조회
     */
    public List<Chore> getUserChoresForSpace(Long userId, Long spaceId) {
        List<SpaceChore> spaceChores = getAllChoresBySpace(spaceId);

        return spaceChores.stream()
                .flatMap(sc -> choreRepository.findBySpaceChoreAndIsDeletedFalse(sc).stream()
                        .filter(chore -> chore.getUserId().equals(userId)))
                .collect(Collectors.toList());
    }

}
