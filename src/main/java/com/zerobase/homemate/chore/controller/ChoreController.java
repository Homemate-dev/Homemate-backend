package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.service.ChoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chore")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreService choreService;

    @PostMapping("/create")
    public ResponseEntity<ChoreDto.Response> createChore(
        @AuthenticationPrincipal UserPrincipal user,
        @Valid @RequestBody ChoreDto.CreateRequest request) {

        ChoreDto.Response response = choreService.createChores(user.id(),
            request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{choreInstanceId}")
    public ResponseEntity<ChoreDto.Response> updateChore(
        @AuthenticationPrincipal UserPrincipal user,
        @PathVariable Long choreInstanceId,
        @Valid @RequestBody ChoreDto.UpdateRequest request) {

        ChoreDto.Response response = choreService.updateChores(user.id(),
            choreInstanceId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
