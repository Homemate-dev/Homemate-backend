package com.zerobase.homemate.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreDto.ApiResponse;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.controller.SpaceController;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceChoreDto;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceChoreCreator;
import com.zerobase.homemate.recommend.service.SpaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SpaceController.class)
@WithMockUser(username = "user1")
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceService spaceService;

    @MockitoBean
    private SpaceChoreCreator spaceChoreCreator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("모든 공간 Parameter 조회")
    void testGetAllSpaces() throws Exception {
        // given
        List<SpaceResponse> mockList = List.of(
                new SpaceResponse("주방", Space.KITCHEN),
                new SpaceResponse("욕실", Space.BATHROOM),
                new SpaceResponse("현관", Space.PORCH)
        );

        when(spaceService.getAllSpaces()).thenReturn(mockList);

        // when & then
        mockMvc.perform(get("/recommend/spaces").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(mockList.size()))
                .andExpect(jsonPath("$[0].spaceName").value("주방"))
                .andExpect(jsonPath("$[1].spaceName").value("욕실"))
                .andExpect(jsonPath("$[2].spaceName").value("현관"));
    }

    @Test
    @DisplayName("특정 공간에 속한 집안일 조회")
    void testGetChoresBySpace() throws Exception {
        // given
        Space space = Space.KITCHEN;

        List<ClassifyChoreResponse> mockChores = List.of(
                new ClassifyChoreResponse(1L, "청소", "매주", space, null),
                new ClassifyChoreResponse(2L, "설거지", "매일", space, null)
        );
        int page = 0;
        when(spaceService.getChoresBySpace(space, page)).thenReturn(mockChores);

        // when & then
        mockMvc.perform(get("/recommend/spaces/{space}/chores", space.name())
                .param("page",  String.valueOf(page))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(mockChores.size()))
                .andExpect(jsonPath("$[0].title").value("청소"))
                .andExpect(jsonPath("$[1].title").value("설거지"));
    }

    @Test
    @DisplayName("SpaceChore 기반 집안일 등록 테스트")
    void createChoreFromSpace_shouldReturnCreatedChore() throws Exception {

        Long spaceChoreId = 10L;

        var principal = new UserPrincipal(1L, "nick", "ROLE_USER");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        // 요청 DTO
        SpaceChoreDto.CreateRequest request = new  SpaceChoreDto.CreateRequest();
        request.setSpace(Space.KITCHEN);

        // Mock Response 생성
        ApiResponse<ChoreDto.Response> mockResponse =
            ApiResponse.<ChoreDto.Response>builder().data(ChoreDto.Response.builder()
                .id(1L)
                .title("주방 싱크대 정리하기")
                .space(request.getSpace())
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .notificationYn(false)
                .build())
            .build();

        when(spaceChoreCreator.createChoreFromSpace(
                eq(1L),
                any(Space.class),
                eq(spaceChoreId)
        )).thenReturn(mockResponse);

        System.out.println(objectMapper.writeValueAsString(mockResponse));

        mockMvc.perform(post("/recommend/spaces/{spaceChoreId}/register", spaceChoreId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(mockResponse.getData().getTitle()))
                .andExpect(jsonPath("$.data.space").value(mockResponse.getData().getSpace().name()))
                .andExpect(jsonPath("$.data.repeatType").value(mockResponse.getData().getRepeatType().name()))
                .andExpect(jsonPath("$.data.repeatInterval").value(mockResponse.getData().getRepeatInterval()));
    }

}
