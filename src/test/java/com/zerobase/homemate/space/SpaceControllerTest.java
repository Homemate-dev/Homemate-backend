package com.zerobase.homemate.space;

import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.controller.SpaceController;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.SpaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

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
    void testGetAllSpaces() throws Exception {
        // SpaceService Mock 세팅
        when(spaceService.getAllSpaces())
                .thenReturn(List.of(Map.of("name", "KITCHEN", "description", "주방")));

        mockMvc.perform(get("/recommend/spaces")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("KITCHEN"))
                .andExpect(jsonPath("$[0].description").value("주방"));
    }

    @Test
    void testGetChoresBySpace() throws Exception {
        // given
        ChoreResponse chore1 = new ChoreResponse(1L, "청소", RepeatType.WEEKLY);
        ChoreResponse chore2 = new ChoreResponse(2L, "설거지", RepeatType.DAILY);

        when(spaceService.getChoresBySpace(Space.KITCHEN))
                .thenReturn(List.of(chore1, chore2));

        // when & then
        mockMvc.perform(get("/recommend/spaces/space/KITCHEN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].choreId").value(1))
                .andExpect(jsonPath("$[0].title").value("청소"))
                .andExpect(jsonPath("$[0].frequency").value("WEEKLY"))
                .andExpect(jsonPath("$[1].choreId").value(2))
                .andExpect(jsonPath("$[1].title").value("설거지"))
                .andExpect(jsonPath("$[1].frequency").value("DAILY"));

        verify(spaceService, times(1)).getChoresBySpace(Space.KITCHEN);
    }
}
