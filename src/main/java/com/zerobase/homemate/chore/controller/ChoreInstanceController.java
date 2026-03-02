package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.chore.service.ChoreService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chore-instances")
@RequiredArgsConstructor
public class ChoreInstanceController {

    private final ChoreService choreService;

    @GetMapping("/{choreInstanceId}")
    public ResponseEntity<ChoreDto.Response> getChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId) {

        ChoreDto.Response response =
                choreService.getChoreByInstanceId(user.id(), choreInstanceId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChoreInstanceDto.Response>> getChoreInstancesByDate(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam LocalDate date) {

        List<ChoreInstanceDto.Response> responses =
                choreService.getChoreInstancesByDate(user.id(), date);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/{choreInstanceId}")
    public ResponseEntity<ApiResponse<ChoreDto.Response>> updateChoreInstance(
        @AuthenticationPrincipal UserPrincipal user,
        @PathVariable Long choreInstanceId,
        @Valid @RequestBody ChoreDto.UpdateRequest request) {

        ApiResponse<ChoreDto.Response> response = choreService.updateChores(user.id(),
            choreInstanceId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{choreInstanceId}/complete")
    public ResponseEntity<ApiResponse<ChoreInstanceDto.Response>> completeChoreInstance(
        @AuthenticationPrincipal UserPrincipal user,
        @PathVariable Long choreInstanceId) {

        ApiResponse<ChoreInstanceDto.Response> response =
            choreService.completeChore(user.id(), choreInstanceId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{choreInstanceId}")
    public ResponseEntity<Void> deleteChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId,
            @RequestParam boolean applyToAfter) {

        choreService.deleteChoreInstance(user.id(), choreInstanceId, applyToAfter);

        return ResponseEntity.noContent().build();
    }
}
