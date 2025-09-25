package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.service.ChoreService;
import com.zerobase.homemate.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chore")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreService choreService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<ChoreDto.Response> createChore(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChoreDto.CreateRequest request) {

        Long userId = jwtUtil.extractUserIdFromToken(authorization);
        ChoreDto.Response response = choreService.createChores(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
