package com.zerobase.homemate.space;

import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.recommend.controller.SpaceController;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SpaceController.class)
@WithMockUser(username = "user1")
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceService spaceService;

    @Test
    @DisplayName("공간 리스트 조회 API 성공")
    void testGetAllSpaces() throws Exception {
        List<SpaceResponse> mockSpaces = List.of(
                new SpaceResponse(1L, "LIVING", "거실", true),
                new SpaceResponse(2L, "KITCHEN", "주방", true)
        );

        when(spaceService.getAllSpaces()).thenReturn(mockSpaces);

        mockMvc.perform(get("/recommend/spaces")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("거실"))
                .andExpect(jsonPath("$[1].code").value("KITCHEN"));
    }

    @Test
    @DisplayName("특정 공간의 랜덤 집안일 조회 API 성공")
    void testGetRandomChores() throws Exception {
        List<SpaceChoreResponse> mockChores = List.of(
                new SpaceChoreResponse(1L, "거실", 1L, "청소기 돌리기", RepeatType.WEEKLY),
                new SpaceChoreResponse(2L, "주방", 2L, "창문 닦기", RepeatType.WEEKLY)
        );

        when(spaceService.getRandomChoresBySpace(1L)).thenReturn(mockChores);

        mockMvc.perform(get("/recommend/spaces/1/chores")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].choreTitle").value("청소기 돌리기"))
                .andExpect(jsonPath("$[1].defaultFreq").value("WEEKLY"));
    }
}
