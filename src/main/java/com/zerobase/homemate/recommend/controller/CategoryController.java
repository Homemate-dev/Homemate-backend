package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}/chores")
    public ResponseEntity<List<ChoreResponse>> getChoresByCategory(
            @PathVariable Long categoryId
    ) {
        List<ChoreResponse> responses = categoryService.getChoresByCategory(categoryId);
        return ResponseEntity.ok(responses);
    }

    // 전체 카테고리 조회
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> responses = categoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }
}
