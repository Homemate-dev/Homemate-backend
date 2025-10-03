package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final ChoreRepository choreRepository;

    // 한 번에 4개씩 나오도록 조정
    public Page<SpaceResponse> getAllSpacesByTop4(int page) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("id").ascending());
        return spaceRepository.findAll(pageable)
                .map(SpaceResponse::fromEntity);
    }


    public SpaceResponse getSpaceByCode(String code) {
        return spaceRepository.findByCodeByTop4(code)
                .map(SpaceResponse::fromEntity)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.VALIDATION_ERROR)
                );
    }

    public SpaceResponse getSpaceById(Long id) {
        return spaceRepository.findById(id)
                .map(SpaceResponse::fromEntity)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.VALIDATION_ERROR)
                );
    }

    public Page<ChoreResponse> getAllChores(int page) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("id").ascending());
        return choreRepository.findAll(pageable)
                .map(ChoreResponse::fromEntity);
    }
}
