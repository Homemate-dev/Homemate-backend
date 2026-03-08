package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.chore.service.ChoreInstanceService;
import com.zerobase.homemate.chore.service.ChoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chore-instances")
@RequiredArgsConstructor
public class ChoreInstanceController {

    private final ChoreService choreService;
    private final ChoreInstanceService choreInstanceService;

    @GetMapping("/{choreInstanceId}")
    public ResponseEntity<ChoreInstanceDto.Response> getChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId
    ) {
        ChoreInstanceDto.Response response = choreInstanceService.getChoreInstance(user.id(), choreInstanceId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChoreInstanceDto.Response>> getChoreInstancesByDate(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam LocalDate date
    ) {
        List<ChoreInstanceDto.Response> responses =
                choreInstanceService.getChoreInstancesByDate(user.id(), date);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/{choreInstanceId}")
    public ResponseEntity<ApiResponse<ChoreDto.Response>> updateChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId,
            @Valid @RequestBody ChoreDto.UpdateRequest request
    ) {
        ApiResponse<ChoreDto.Response> response = choreService.updateChores(user.id(),
                choreInstanceId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{choreInstanceId}/complete")
    public ResponseEntity<ApiResponse<ChoreInstanceDto.Response>> completeChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId
    ) {
        ApiResponse<ChoreInstanceDto.Response> response = choreInstanceService.patchCompletionStatus(user.id(), choreInstanceId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{choreInstanceId}")
    public ResponseEntity<Void> deleteChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId
    ) {
        choreInstanceService.deleteChoreInstance(user.id(), choreInstanceId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<LocalDate>> getCalendarMarks(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<LocalDate> dates =
                choreService.getCalendarMarkedDates(user.id(), startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(dates);
    }

    @GetMapping("/completion-rate")
    public ResponseEntity<Map<String, Double>> getCompletionRate(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam LocalDate date
    ) {
        double rate = choreService.getTodayCompleteRate(user.id(), date);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("rate", rate));
    }
}
