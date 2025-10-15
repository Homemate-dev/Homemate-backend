package com.zerobase.homemate.recommend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.recommend.controller.CategoryController;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@WithMockUser(username = "testUser")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("카테고리 전체 조회 API 성공")
    @Test
    void getAllCategories_success() throws Exception {
        List<CategoryResponse> mockResponse = List.of(
                new CategoryResponse("겨울철 대맞이 청소"),
                new CategoryResponse("하루 15분 청소")
        );

        Mockito.when(categoryService.getAllCategories()).thenReturn(mockResponse);

        mockMvc.perform(get("/recommend/categories")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("겨울철 대맞이 청소"))
                .andExpect(jsonPath("$[1].category").value("하루 15분 청소"));

    }

    @DisplayName("✅ 특정 카테고리별 집안일 조회 API 성공")
    @Test
    void getChoresByCategory_success() throws Exception {
        // frequency 필드에 맞춰서 Mock 데이터 생성
        List<ClassifyChoreResponse> mockResponse = List.of(
                new ClassifyChoreResponse(1L, "설거지", "매일", null, "겨울철 대맞이 청소"),
                new ClassifyChoreResponse(2L, "냉장고 청소", "매달", null,"겨울철 대맞이 청소")
        );

        Mockito.when(categoryService.getChoresByCategory(eq(Category.WINTER)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/recommend/categories/WINTER/chores")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("설거지"))
                .andExpect(jsonPath("$[0].frequency").value("매일"))
                .andExpect(jsonPath("$[1].title").value("냉장고 청소"))
                .andExpect(jsonPath("$[1].frequency").value("매달"));
    }

}
