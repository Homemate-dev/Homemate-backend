package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reco/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping("/{spaceId}/chores")
    public Page<SpaceResponse> getSpacesList(@RequestParam(defaultValue = "0") int page) {
        return spaceService.getAllSpacesByTop4(page);
    }

    @GetMapping("/{spaceId}")
    public SpaceResponse getSpaceById(@PathVariable Long id) {
        return spaceService.getSpaceById(id);
    }

    @GetMapping("/{spaceCode}")
    public SpaceResponse getSpaceByCode(@PathVariable String code) {
        return spaceService.getSpaceByCode(code);
    }
}
