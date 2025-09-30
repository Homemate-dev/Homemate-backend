package com.zerobase.homemate.recommend.controller;

import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reco/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getChoresByCategory(@PathVariable Category category) {
        List<ChoreResponse> responses = categoryService.getChoresByCategory(category);

        Map<String, Object> result = new HashMap<>();
        result.put("data", responses);
        result.put("error", null);

        return ResponseEntity.ok(result);
    }
}
