package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final ChoreStatsService choreStatsService;

    @GetMapping("/random")
    public ResponseEntity<List<SpaceChoreResponse>> findRandomChores() {
        List<SpaceChoreResponse> chores = recommendService.getRandomChores();
        return ResponseEntity.ok(chores);
    }

    @GetMapping("/trend")
    public List<TopItemDto> getTopOverall(@RequestParam(defaultValue = "5") int topN){
        return choreStatsService.getTopOverallWithMissions(topN);
    }
}
