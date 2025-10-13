package com.zerobase.homemate.recommend;

import com.zerobase.homemate.recommend.controller.CategoryController;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@WithMockUser(username = "testUser")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 ID로 집안일 목록 조회 성공")
    void getChoresByCategory_success() throws Exception {
        // given
        List<ChoreResponse> mockChores = List.of(
                new ChoreResponse(1L, "거실 청소하기", "매주"),
                new ChoreResponse(2L, "욕실 청소하기", "2주")
        );
        Mockito.when(categoryService.getChoresByCategory(anyLong()))
                .thenReturn(mockChores);

        // when & then
        mockMvc.perform(get("/recommend/categories/1/chores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("거실 청소하기"))
                .andExpect(jsonPath("$[1].title").value("욕실 청소하기"))
                .andExpect(jsonPath("$[0].frequency").value("매주"));
    }

    @Test
    @DisplayName("전체 카테고리 조회 성공")
    void getAllCategories_success() throws Exception {
        // given
        List<CategoryResponse> mockCategories = List.of(
                new CategoryResponse("청소"),
                new CategoryResponse("요리")
        );
        Mockito.when(categoryService.getAllCategories())
                .thenReturn(mockCategories);

        // when & then
        mockMvc.perform(get("/recommend/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("청소"))
                .andExpect(jsonPath("$[1].name").value("요리"));
    }
}
