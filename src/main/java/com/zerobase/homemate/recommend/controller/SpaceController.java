package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceChoreDto;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceChoreCreator;
import com.zerobase.homemate.recommend.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/recommend/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;
    private final SpaceChoreCreator spaceChoreCreator;

    @GetMapping
    public ResponseEntity<List<SpaceResponse>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }

    @GetMapping("/{space}/chores")
    public ResponseEntity<List<ClassifyChoreResponse>> getChoresBySpace(
            @PathVariable("space") Space space,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(spaceService.getChoresBySpace(space, page));
    }

    @PostMapping("/{spaceChoreId}/register")
    public ResponseEntity<ChoreDto.Response> createChoreFromSpace(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long spaceChoreId,
            @RequestBody SpaceChoreDto.CreateRequest request
    ){
        ChoreDto.Response response = spaceChoreCreator.createChoreFromSpace(
                userPrincipal.id(),
                request.getSpace(),
                spaceChoreId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
