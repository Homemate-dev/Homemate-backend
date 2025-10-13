package com.zerobase.homemate.mission.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.mission.service.MissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping()
    public ResponseEntity<List<MissionDto.Response>> getMonthlyMissions(
        @AuthenticationPrincipal UserPrincipal user) {

        List<MissionDto.Response> missions =
            missionService.getMonthlyMissions(user.id());

        return ResponseEntity.status(HttpStatus.OK).body(missions);
    }
}
