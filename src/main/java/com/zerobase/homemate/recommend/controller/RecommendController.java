package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/random")
    public ResponseEntity<List<SpaceChoreResponse>> findRandomChores() {
        List<SpaceChoreResponse> chores = recommendService.getRandomChores();
        return ResponseEntity.ok(chores);
    }
}
