package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.chore.service.ChoreService;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 공간별 집안일 전체 조회
    public List<SpaceChore> getAllChoresBySpace(Long spaceId){
        Space space = spaceRepository.findByIdAndIsActiveTrue(spaceId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPACE_NOT_FOUND));

        return spaceChoreRepository.findBySpaceAndIsActiveTrue(space);
    }

    // 공간별 랜덤 집안일 추천(최대 4개)
    public List<SpaceChore> getRandomChoresBySpace(Long spaceId){
        int count = 4;

        List<SpaceChore> choreList = getAllChoresBySpace(spaceId);
        if(choreList.isEmpty()){
            return Collections.emptyList();
        }

        // choreList에서 4개 랜덤 추출
        return choreList.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(), list ->{
                            Collections.shuffle(list, random);
                            return list.stream().limit(count).collect(Collectors.toList());
                        }
                ));
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
