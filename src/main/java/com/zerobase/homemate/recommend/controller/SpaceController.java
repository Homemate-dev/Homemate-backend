package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
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

    @GetMapping
    public ResponseEntity<List<SpaceResponse>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }

    @GetMapping("/{space}/chores")
    public ResponseEntity<List<ClassifyChoreResponse>> getChoresBySpace(
            @PathVariable("space") Space space) {
        return ResponseEntity.ok(spaceService.getChoresBySpace(space));
    }



}
