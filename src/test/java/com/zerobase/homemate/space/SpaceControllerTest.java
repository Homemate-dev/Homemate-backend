package com.zerobase.homemate.space;

import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.controller.SpaceController;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
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
import static org.mockito.Mockito.*;
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

        when(spaceService.getChoresBySpace(space)).thenReturn(mockChores);

        // when & then
        mockMvc.perform(get("/recommend/spaces/{space}/chores", space.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(mockChores.size()))
                .andExpect(jsonPath("$[0].title").value("청소"))
                .andExpect(jsonPath("$[1].title").value("설거지"));
    }
}
