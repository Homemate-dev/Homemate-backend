package com.zerobase.homemate.recommend.service;


import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final SpaceChoreRepository spaceChoreRepository;

    public List<SpaceChoreResponse> getRandomChores(){
        return spaceChoreRepository.findRandomChores();
    }
}
