package com.zerobase.homemate.chores.controller;

import com.zerobase.homemate.chores.dto.ChoresDto;
import com.zerobase.homemate.chores.service.ChoresService;
import com.zerobase.homemate.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chores")
@RequiredArgsConstructor
public class ChoresController {

    private final ChoresService choresService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<ChoresDto.Response> createChores(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChoresDto.CreateRequest request) {

        Long userId = jwtUtil.extractUserIdFromToken(authorization);
        ChoresDto.Response response = choresService.createChores(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
