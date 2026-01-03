package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Season;
import com.zerobase.homemate.entity.enums.SubCategory;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
import com.zerobase.homemate.recommend.service.CategoryQueryService;
import com.zerobase.homemate.recommend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/recommend/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryChoreCreator categoryChoreCreator;
    private final CategoryQueryService categoryQueryService;

    // 전체 집안일에서 카테고리 필터링
    @GetMapping("/{category}/chores")
    public ResponseEntity<List<ClassifyChoreResponse>> getChoresByCategory(
            @PathVariable Category category
            ) {
        List<ClassifyChoreResponse> responses = categoryService.getChoresByCategory(category);
        return ResponseEntity.ok(responses);
    }

    // 전체 카테고리 조회
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> responses = categoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{categoryChoreId}/register")
    public ResponseEntity<ChoreDto.ApiResponse<ChoreDto.Response>> createChoreFromCategory(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long categoryChoreId
    ) {
        ChoreDto.ApiResponse<ChoreDto.Response> response = categoryChoreCreator.createChoreFromCategory(
                user.id(),
                categoryChoreId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 고정 카테고리 조회 API
    @GetMapping("/fixed/{category}")
    public ResponseEntity<List<ClassifyChoreResponse>> getChoresByFixedCategory(
            @PathVariable Category category
    ){
        List<ClassifyChoreResponse> responses = categoryQueryService.getFixedChores(category);
        return ResponseEntity.ok(responses);
    }

    // 계절 카테고리 조회 API
    @GetMapping("/season")
    public ResponseEntity<List<ClassifyChoreResponse>> getChoresBySeason(
    ){
        Season currentSeason = Season.from(LocalDate.now(ZoneId.of("Asia/Seoul")));

        return ResponseEntity.ok(
                categoryQueryService.getSeasonChores(currentSeason)
        );
    }

    // 월간 카테고리 조회 API
    @GetMapping("/monthly/{categoryId}/chores")
    public ResponseEntity<List<ClassifyChoreResponse>> getChoresByMonthlyCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false)SubCategory subCategory

            ){
        return ResponseEntity.ok(
                categoryQueryService.getMonthlyChores(categoryId, subCategory)
        );
    }
}
