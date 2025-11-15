//package com.zerobase.homemate.recommend;
//
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.zerobase.homemate.auth.service.JwtService;
//import com.zerobase.homemate.entity.*;
//import com.zerobase.homemate.entity.enums.*;
//import com.zerobase.homemate.notification.controller.NotionWebhookController;
//import com.zerobase.homemate.repository.*;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.MediaType;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.YearMonth;
//import java.util.HashMap;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//public class RecommendChoreScenarioTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private MissionRepository missionRepository;
//
//    @Autowired
//    private UserMissionRepository userMissionRepository;
//
//    @Autowired
//    private ChoreRepository choreRepository;
//
//    @Autowired
//    private ChoreInstanceRepository choreInstanceRepository;
//
//    @Autowired
//    private SpaceChoreRepository spaceChoreRepository;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private NotionWebhookController notionWebhookController;
//
//    @Test
//    @DisplayName("집안일 등록 시 - 10 회 미션이 완료되면 missionResult가 응답한다")
//    void createChore_shouldReturnMissionResult_whenMissionCompleted() throws Exception {
//        // --- 1️⃣ 유저 생성 ---
//        User user = userRepository.save(User.builder()
//                .profileName("테스트유저")
//                .userRole(UserRole.USER)
//                .userStatus(UserStatus.ACTIVE)
//                .build());
//
//        String sid = UUID.randomUUID().toString();
//
//        String accessToken = jwtService.createAccessToken(user, sid);
//
//        // --- 2️⃣ 10회 미션 생성 ---
//        Mission mission = missionRepository.save(Mission.builder()
//                .missionType(MissionType.USER_ACTION)
//                .userActionType(UserActionType.CREATE_CHORE_WITH_SPACE)
//                .title("주방 집안일 등록 10회")
//                .space(Space.KITCHEN)
//                .activeYearMonth(YearMonth.now())
//                .targetCount(10)
//                .isActive(true)
//                .build());
//
//        // --- 3️⃣ UserMission(누적 9회) 생성 ---
//        UserMission userMission = userMissionRepository.save(UserMission.builder()
//                .user(user)
//                .mission(mission)
//                .currentCount(9)
//                .build());
//
//        // --- 4️⃣ 기존 집안일을 9개 생성해둔다 (실제 MissionService는 count만 보고 판단함) ---
//        for (int i = 1; i <= 9; i++) {
//            Chore c = choreRepository.save(Chore.builder()
//                    .user(user)
//                    .title("기존 주방 집안일 " + i)
//                    .space(Space.KITCHEN)
//                    .repeatType(RepeatType.NONE)
//                    .notificationYn(false)
//                            .startDate(LocalDate.now())
//                            .endDate(LocalDate.now().plusDays(i))
//                    .isDeleted(false)
//                    .build());
//
//            choreInstanceRepository.save(
//                    ChoreInstance.builder()
//                            .chore(c)
//                            .titleSnapshot("기존 주방 Instance " + i)
//                            .dueDate(LocalDate.now())
//                            .choreStatus(ChoreStatus.PENDING)
//                            .createdAt(LocalDateTime.now())
//                            .build()
//            );
//        }
//
//        SpaceChore oneChore = spaceChoreRepository.save(
//                SpaceChore.builder()
//                        .titleKo("싱크대 거름망 비우기")
//                        .repeatType(RepeatType.DAILY)
//                        .repeatInterval(1)
//                        .space(Space.KITCHEN)
//                        .code("주방")
//                        .build()
//        );
//
//        // --- 마지막 SpaceChore 기반 집안일 생성 요청 DTO ---
//        SpaceChore lastSpaceChore = spaceChoreRepository.findByTitleKo("싱크대 거름망 비우기").orElseThrow();
//
//        var request = new HashMap<String, Object>();
//        request.put("spaceChoreId", lastSpaceChore.getId());  // SpaceChore 기준
//        request.put("title", lastSpaceChore.getTitleKo());       // 제목도 SpaceChore 기준
//        request.put("space", lastSpaceChore.getSpace().name());
//        request.put("repeatType", "NONE");
//        request.put("notificationYn", false);
//
//        String json = objectMapper.writeValueAsString(request);
//
//
//
//        // --- 6️⃣ API 호출 ---
//        MvcResult result = mockMvc.perform(post("/recommend/spaces/{spaceChoreId}/register", lastSpaceChore.getId())
//                        .header("Authorization", "Bearer " + accessToken)
//                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
//                        .content(json)
//                        .header("X-USER-ID", user.getId()) // 인증방식에 맞게 수정
//                )
//                .andExpect(status().isCreated())
//                .andReturn();
//
//
//        // --- 7️⃣ 응답 파싱 ---
//        String response = result.getResponse().getContentAsString();
//
//        JsonNode root = objectMapper.readTree(response);
//
//        JsonNode missionResults = root.get("missionResults");
//
//
//        // --- 8️⃣ Assertions ---
//        assertNotNull(missionResults, "missionResults 필드가 있어야 함");
//        assertTrue(missionResults.isArray());
//        assertEquals(1, missionResults.size(), "미션이 완료되었으므로 1건이 반환되어야 함");
//
//        JsonNode missionNode = missionResults.get(0);
//
//        assertEquals("주방 집안일 등록 10회", missionNode.get("title").asText());
//        assertEquals(10, missionNode.get("currentCount").asInt());
//        assertEquals(10, missionNode.get("targetCount").asInt());
//        assertTrue(missionNode.get("completed").asBoolean(), "미션 완료 플래그가 true여야 함");
//    }
//}
