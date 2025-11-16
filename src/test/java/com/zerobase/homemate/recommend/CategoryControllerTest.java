package com.zerobase.homemate.recommend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.recommend.controller.CategoryController;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@WithMockUser(username = "testUser")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryChoreCreator categoryChoreCreator;

    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("카테고리 전체 조회 API 성공")
    @Test
    void getAllCategories_success() throws Exception {
        List<CategoryResponse> mockResponse = List.of(
                new CategoryResponse("겨울철 대맞이 청소"),
                new CategoryResponse("하루 15분 청소")
        );

        when(categoryService.getAllCategories()).thenReturn(mockResponse);

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

        when(categoryService.getChoresByCategory(eq(Category.WINTER)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/recommend/categories/WINTER/chores")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("설거지"))
                .andExpect(jsonPath("$[0].frequency").value("매일"))
                .andExpect(jsonPath("$[1].title").value("냉장고 청소"))
                .andExpect(jsonPath("$[1].frequency").value("매달"));
    }

    @Test
    @DisplayName("CategoryChore 기반 집안일 등록 테스트")
    void createChoreFromCategory_shouldReturnCreatedChore() throws Exception {

        Long categoryChoreId = 1L;

        var principal = new UserPrincipal(1L, "nick", "ROLE_USER");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());


        ChoreDto.Response responseData = ChoreDto.Response.builder()
                .id(100L)
                .title("청소하기")
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .notificationYn(true)
                .notificationTime(LocalTime.of(9, 0))
                .build();

        ChoreDto.ApiResponse<ChoreDto.Response> apiResponse =
                ChoreDto.ApiResponse.<ChoreDto.Response>builder()
                        .data(responseData)
                        .build();

        when(categoryChoreCreator.createChoreFromCategory(principal.id(), categoryChoreId))
                .thenReturn(apiResponse);


        mockMvc.perform(post("/recommend/categories/{categoryChoreId}/register", categoryChoreId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("청소하기"))
                .andExpect(jsonPath("$.data.repeatType").value("DAILY"))
                .andExpect(jsonPath("$.data.repeatInterval").value(3))
                .andExpect(jsonPath("$.data.notificationTime").value("09:00:00"));
    }



}
