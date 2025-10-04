package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;

import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/recommend/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    // 1. 공간 리스트 조회
    @GetMapping
    public ResponseEntity<List<SpaceResponse>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }


    @GetMapping("/{spaceId}/chores")
    public ResponseEntity<List<SpaceChoreResponse>> getRandomChores(@PathVariable Long spaceId) {
        List<SpaceChoreResponse> recommendChores = spaceService.getRandomChoresBySpace(spaceId);
        return ResponseEntity.ok(recommendChores);
    }

    /**
     * 사용자별 SpaceChore 기준 실제 Chore 인스턴스 조회
     */
    @GetMapping("/{spaceId}/chores/user/{user}")
    public ResponseEntity<List<Chore>> getUserChores(@PathVariable Long spaceId,
                                                     @PathVariable User user) {
        List<Chore> userChores = spaceService.getUserChoresForSpace(user, spaceId);
        return ResponseEntity.ok(userChores);
    }




}
