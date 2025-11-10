package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
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
            @PathVariable("space") Space space) {
        return ResponseEntity.ok(spaceService.getChoresBySpace(space));
    }

    @PostMapping("/{spaceChoreId}/register")
    public ResponseEntity<List<ChoreInstanceDto.Response>> createChoreFromSpace(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long spaceChoreId,
            @RequestBody SpaceChoreDto.CreateRequest request
    ) {
        List<ChoreInstanceDto.Response> response =
                spaceChoreCreator.createChoreFromSpace(user.id(), request.getSpace(), spaceChoreId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{spaceChoreId}")
    public ResponseEntity<SpaceChoreDto.Response> getSpaceChore(
        @AuthenticationPrincipal UserPrincipal user,
        @PathVariable Long spaceChoreId) {
        return ResponseEntity.ok(
            spaceService.getSpaceChore(user.id(), spaceChoreId));
    }
}
