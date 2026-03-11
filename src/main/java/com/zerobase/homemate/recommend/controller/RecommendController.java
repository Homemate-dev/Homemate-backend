package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.recommend.dto.MonthlyRecommendResponse;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/total")
    public List<TopItemDto> getTopCategories(
            @AuthenticationPrincipal UserPrincipal user
    ){
        return choreStatsService.getTopCategories(user.id());
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyRecommendResponse>> getMonthlyCategories(){
        return ResponseEntity.ok(recommendService.getMonthlyCategories());
    }
}
