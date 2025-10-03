package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
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

    @GetMapping("/{spaceId}/chores")
    public ResponseEntity<List<SpaceChore>> getRandomChores(@PathVariable Long spaceId) {
        List<SpaceChore> recommendChores = spaceService.getRandomChoresBySpace(spaceId);
        return ResponseEntity.ok(recommendChores);
    }

    /**
     * 사용자별 SpaceChore 기준 실제 Chore 인스턴스 조회
     */
    @GetMapping("/{spaceId}/chores/user/{userId}")
    public ResponseEntity<List<Chore>> getUserChores(@PathVariable Long spaceId,
                                                     @PathVariable Long userId) {
        List<Chore> userChores = spaceService.getUserChoresForSpace(userId, spaceId);
        return ResponseEntity.ok(userChores);
    }




}
