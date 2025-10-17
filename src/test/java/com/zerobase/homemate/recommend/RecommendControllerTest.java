package com.zerobase.homemate.recommend;

import com.zerobase.homemate.recommend.controller.RecommendController;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.service.RecommendService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendController.class)
@WithMockUser(username = "tester", roles = "USER")
public class RecommendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendService recommendService;

    @Test
    void getRandomChores_shouldReturnThreeChores() throws Exception {
        // given - 익명 클래스 방식으로 두 필드 구현
        SpaceChoreResponse sc1 = new SpaceChoreResponse() {
            public Long getId() { return 1L; }
            public String getTitleKo() { return "주방 싱크대 청소하기"; }
        };
        SpaceChoreResponse sc2 = new SpaceChoreResponse() {
            public Long getId() { return 2L; }
            public String getTitleKo() { return "신발정리"; }
        };
        SpaceChoreResponse sc3 = new SpaceChoreResponse() {
            public Long getId() { return 3L; }
            public String getTitleKo() { return "환기하기"; }
        };

        Mockito.when(recommendService.getRandomChores()).thenReturn(List.of(sc1, sc2, sc3));

        mockMvc.perform(get("/recommend/random")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titleKo").value("주방 싱크대 청소하기"))
                .andExpect(jsonPath("$[1].titleKo").value("신발정리"))
                .andExpect(jsonPath("$[2].titleKo").value("환기하기"));

    }
}
