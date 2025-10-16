package com.zerobase.homemate.recommend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.*;
import com.zerobase.homemate.recommend.controller.CategoryController;
import com.zerobase.homemate.recommend.dto.CategoryChoreDto;
import com.zerobase.homemate.recommend.dto.CategoryResponse;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @WithMockCustomUser(id = 100L)
    void createChoreFromCategory_shouldReturnCreatedChore() throws Exception {

        Long categoryChoreId = 1L;
        User mockUser = User.builder()
                .id(100L)
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .userRole(UserRole.USER)
                .build();

        // 요청 DTO
        CategoryChoreDto.CreateRequest request = new CategoryChoreDto.CreateRequest();
        request.setCategory(Category.WINTER);

        // Mock Response 생성
        ChoreDto.Response mockResponse = ChoreDto.Response.builder()
                .id(1L) // 필수
                .title("청소하기")
                .space(Space.KITCHEN)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .notificationYn(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Service Mock
        when(categoryChoreCreator.createChoreFromCategory(
                eq(100L),
                any(Category.class),
                eq(categoryChoreId)
        )).thenReturn(mockResponse);

        System.out.println(objectMapper.writeValueAsString(mockResponse));


        // SecurityContext에 userId를 주입
        mockMvc.perform(post("/recommend/categories/{categoryChoreId}/register", categoryChoreId)
                        .with(csrf())
                        .with(request1 -> {
                            // Authentication에 userId 세팅
                            request1.setUserPrincipal(() -> String.valueOf(mockUser.getId()));
                            return request1;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(mockResponse.getTitle()))
                .andExpect(jsonPath("$.space").value(mockResponse.getSpace().name()))
                .andExpect(jsonPath("$.repeatType").value(mockResponse.getRepeatType().name()))
                .andExpect(jsonPath("$.repeatInterval").value(mockResponse.getRepeatInterval()));
    }


}
