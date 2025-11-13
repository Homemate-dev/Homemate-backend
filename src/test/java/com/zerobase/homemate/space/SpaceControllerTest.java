package com.zerobase.homemate.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.auth.security.UserPrincipal;
import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.chore.dto.ChoreInstanceDto;
import com.zerobase.homemate.entity.enums.ChoreStatus;
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
import java.time.LocalTime;
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
        when(spaceService.getChoresBySpace(space)).thenReturn(mockChores);

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

        SpaceChoreDto.CreateRequest request = new SpaceChoreDto.CreateRequest();
        request.setSpace(Space.KITCHEN);

        // Mock Response 생성
        ChoreInstanceDto.Response instance1 = ChoreInstanceDto.Response.builder()
                .id(1L)
                .choreId(100L)
                .titleSnapshot("주방 싱크대 정리하기")
                .dueDate(LocalDate.now())
                .notificationTime(LocalTime.of(9, 0))
                .choreStatus(ChoreStatus.PENDING)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .createdAt(LocalDateTime.now())
                .build();

        ChoreInstanceDto.Response instance2 = ChoreInstanceDto.Response.builder()
                .id(2L)
                .choreId(101L)
                .titleSnapshot("주방 바닥 청소하기")
                .dueDate(LocalDate.now())
                .notificationTime(LocalTime.of(19, 0))
                .choreStatus(ChoreStatus.PENDING)
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .createdAt(LocalDateTime.now())
                .build();

        List<ChoreInstanceDto.Response> mockResponse = List.of(instance1, instance2);

        // ApiResponse로 감싸기
        ChoreDto.ApiResponse<List<ChoreInstanceDto.Response>> apiResponse =
                ChoreDto.ApiResponse.<List<ChoreInstanceDto.Response>>builder()
                        .data(mockResponse)
                        .missionResults(List.of())
                        .build();

        when(spaceChoreCreator.createChoreFromSpace(
                eq(principal.id()),
                eq(request.getSpace()),
                eq(spaceChoreId)
        )).thenReturn(apiResponse);

        // MockMvc 수행
        mockMvc.perform(post("/recommend/spaces/{spaceChoreId}/register", spaceChoreId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].titleSnapshot").value("주방 싱크대 정리하기"))
                .andExpect(jsonPath("$.data[0].repeatType").value("DAILY"))
                .andExpect(jsonPath("$.data[0].choreStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[1].titleSnapshot").value("주방 바닥 청소하기"));
    }


}
