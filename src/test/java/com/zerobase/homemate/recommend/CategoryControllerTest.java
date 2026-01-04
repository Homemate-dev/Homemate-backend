package com.zerobase.homemate.recommend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.recommend.controller.CategoryController;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
import com.zerobase.homemate.recommend.service.CategoryQueryService;
import com.zerobase.homemate.recommend.service.MonthlyCategoryService;
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
import static org.mockito.BDDMockito.given;
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
    private MonthlyCategoryService monthlyCategoryService;
    
    @MockitoBean
    private CategoryQueryService categoryQueryService;

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

        when(categoryQueryService.getAllCategories()).thenReturn(mockResponse);

        mockMvc.perform(get("/recommend/categories")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("겨울철 대맞이 청소"))
                .andExpect(jsonPath("$[1].category").value("하루 15분 청소"));

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

    @Test
    @DisplayName("고정 카테고리 조회 성공")
    void getFixedCategoryChores() throws Exception {
        // given
        Category category = Category.APPLIANCE_MAINTENANCE;

        ClassifyChoreResponse response =
                new ClassifyChoreResponse(
                        1L,
                        "가습기 세척하기",
                        "1",
                        null,
                        Category.APPLIANCE_MAINTENANCE.getCategoryName()
                );

        given(categoryQueryService.getFixedChores(category))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/recommend/categories/fixed/{category}", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("가습기 세척하기"));

    }

    @Test
    @DisplayName("계절 카테고리 조회 성공")
    void getSeasonCategoryChores() throws Exception {
        // given

        ClassifyChoreResponse response =
                new ClassifyChoreResponse(
                        1L,
                        "필터 교체하기",
                        "1",
                        null,
                        Season.WINTER.name()
                );
        given(categoryQueryService.getSeasonChores(any()))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/recommend/categories/season"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("필터 교체하기"));
    }

    @Test
    @DisplayName("월간 카테고리 집안일 조회 성공")
    void getMonthlyCategoryChores() throws Exception {
        // given
        Long categoriesId = 1L;

        ClassifyChoreResponse response =
                new ClassifyChoreResponse(
                        1L,
                        "보일러 점검하기",
                        "1",
                        null,
                        "1월 대청소"
                );

        given(categoryQueryService.getMonthlyChores(1L, null))
                .willReturn(List.of(response));

        mockMvc.perform(get("/recommend/categories/monthly/{categoryId}/chores", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("1월 대청소"))
                .andExpect(jsonPath("$[0].title").value("보일러 점검하기"));
    }
}
