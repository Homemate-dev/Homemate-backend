package com.zerobase.homemate.chore.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.chore.service.ChoreService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chores")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreService choreService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChoreDto.Response>> createChore(
        @AuthenticationPrincipal UserPrincipal user,
        @Valid @RequestBody ChoreDto.CreateRequest request) {

        ApiResponse<ChoreDto.Response> response =
            choreService.createChores(user.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{choreId}")
    public ResponseEntity<ChoreDto.Response> getChore(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreId) {

        ChoreDto.Response response =
                choreService.getChoreByChoreId(user.id(), choreId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{choreId}")
    public ResponseEntity<Void> deleteChore(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long choreId) {

        choreService.deleteChore(user.id(), choreId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<LocalDate>> getCalendarMarks(
        @AuthenticationPrincipal UserPrincipal user,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {

        List<LocalDate> dates =
            choreService.getCalendarMarkedDates(user.id(), startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(dates);
    }

    @GetMapping("/completion-rate/{date}")
    public ResponseEntity<Map<String, Double>> getCompletionRate(
        @AuthenticationPrincipal UserPrincipal user,
        @PathVariable LocalDate date
    ) {

        double rate = choreService.getTodayCompleteRate(user.id(), date);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("rate", rate));
    }

    @GetMapping
    public ResponseEntity<List<ChoreDto.Response>> getChoreList(
        @AuthenticationPrincipal UserPrincipal user,
        @RequestParam String filter,
        @RequestParam(required = false) String space,
        @RequestParam(required = false) String repeat,
        @RequestParam(required = false) Integer repeatInterval,
        @RequestParam(required = false) String status
    ) {

        List<ChoreDto.Response> chores =
            choreService.getChoreList(user.id(), filter, space,
                    repeat, repeatInterval, status);

        return ResponseEntity.status(HttpStatus.OK).body(chores);
    }
}
