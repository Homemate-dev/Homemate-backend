package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/recommend/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }

    @GetMapping("/{space}/chores")
    public ResponseEntity<List<ChoreResponse>> getChoresBySpace(
            @PathVariable("space") Space space) {
        return ResponseEntity.ok(spaceService.getChoresBySpace(space));
    }



}
