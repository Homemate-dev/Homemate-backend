package com.zerobase.homemate.space;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.recommend.controller.SpaceController;
import com.zerobase.homemate.recommend.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SpaceController.class)
@WithMockUser(username = "user1")
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Service를 Mock으로 등록 → 실제 DB 필요 없음
    @MockitoBean
    private SpaceService spaceService;

    private SpaceChore chore1;
    private SpaceChore chore2;

    @BeforeEach
    void setUp() {
        chore1 = SpaceChore.builder().id(1L).titleKo("청소").build();
        chore2 = SpaceChore.builder().id(2L).titleKo("정리").build();
    }

    @Test
    void testGetAllChores() throws Exception {
        // Service 호출 시 Mock 데이터 반환
        Mockito.when(spaceService.getRandomChoresBySpace(anyLong()))
                .thenReturn(Arrays.asList(chore1, chore2));

        mockMvc.perform(get("/recommend/spaces/1/chores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titleKo").value("청소"))
                .andExpect(jsonPath("$[1].titleKo").value("정리"));
    }

    @Test
    void testGetUserChores() throws Exception {
        Mockito.when(spaceService.getUserChoresForSpace(anyLong(), anyLong()))
                .thenReturn(Arrays.asList()); // 사용자별 Chore는 빈 리스트로 테스트

        mockMvc.perform(get("/recommend/spaces/1/chores/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
