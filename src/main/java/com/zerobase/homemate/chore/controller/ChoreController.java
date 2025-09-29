package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.service.ChoreService;

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


}
