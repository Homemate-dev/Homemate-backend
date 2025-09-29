package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.chore.service.ChoreService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chore")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreService choreService;


}
