package com.zerobase.homemate.recommend.service;


import com.zerobase.homemate.recommend.dto.MonthlyRecommendResponse;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.repository.CategoriesRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final SpaceChoreRepository spaceChoreRepository;
    private final CategoriesRepository categoriesRepository;

    public List<SpaceChoreResponse> getRandomChores(){
        return spaceChoreRepository.findRandomChores();
    }

    public List<MonthlyRecommendResponse> getMonthlyCategories(){

        YearMonth now = YearMonth.now(ZoneId.of("Asia/Seoul"));
        String targetMonth = now.toString();

        return categoriesRepository.findActiveMonthlyByTargetMonth(targetMonth)
                .stream()
                .map(MonthlyRecommendResponse::from)
                .toList();
    }
}
