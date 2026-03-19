package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreCompletionRateResponse;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.chore.service.ChoreInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/chore-instances")
@RequiredArgsConstructor
public class ChoreInstanceController {

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
        List<ChoreInstanceDto.Response> responses = choreInstanceService.getChoreInstancesByDate(user.id(), date);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/{choreInstanceId}")
    public ResponseEntity<ChoreInstanceDto.Response> updateChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId,
            @RequestBody @Valid ChoreInstanceDto.Request request
    ) {
        ChoreInstanceDto.Response response = choreInstanceService.updateChoreInstance(user.id(), choreInstanceId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PatchMapping("/{choreInstanceId}/complete")
    public ResponseEntity<ApiResponse<ChoreInstanceDto.Response>> completeChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId
    ) {
        ApiResponse<ChoreInstanceDto.Response> response = choreInstanceService.completeInstance(user.id(), choreInstanceId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{choreInstanceId}/incomplete")
    public ResponseEntity<ChoreInstanceDto.Response> incompleteChoreInstance(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreInstanceId
    ) {
        ChoreInstanceDto.Response response = choreInstanceService.undoInstanceCompletion(user.id(), choreInstanceId);

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
        List<LocalDate> response = choreInstanceService.getCalendarMarkedDates(user.id(), startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/completion-rate")
    public ResponseEntity<ChoreCompletionRateResponse> getCompletionRate(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam LocalDate date
    ) {
        ChoreCompletionRateResponse response = choreInstanceService.getChoreCompletionRate(user.id(), date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
