package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.recommend.dto.CategoryChoreDto;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
import com.zerobase.homemate.recommend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryChoreCreator categoryChoreCreator;

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
    public ResponseEntity<ChoreDto.ApiResponse<List<ChoreInstanceDto.Response>>> createChoreFromCategory(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long categoryChoreId,
            @RequestBody CategoryChoreDto.CreateRequest request
    ) {
        ChoreDto.ApiResponse<List<ChoreInstanceDto.Response>> response = categoryChoreCreator.createChoreFromCategory(
                user.id(),
                request.getCategory(),
                categoryChoreId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




}
