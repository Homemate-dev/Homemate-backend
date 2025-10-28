//package com.zerobase.homemate.badge;
//
//
//import com.zerobase.homemate.auth.service.JwtService;
//import com.zerobase.homemate.badge.service.BadgeService;
//import com.zerobase.homemate.entity.User;
//import com.zerobase.homemate.entity.enums.BadgeType;
//import com.zerobase.homemate.entity.enums.UserRole;
//import com.zerobase.homemate.entity.enums.UserStatus;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class BadgeControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @MockitoBean
//    private BadgeService badgeService;
//
//    @Test
//    @DisplayName("JWT 인증을 사용해 획득 배지 조회")
//    void testGetAcquiredBadgesWithJwt() throws Exception{
//        // Mock user
//        User mockUser = User.builder()
//                .id(1L)
//                .profileName("test")
//                .userRole(UserRole.USER)
//                .userStatus(UserStatus.ACTIVE)
//                .build();
//
//        String sid = UUID.randomUUID().toString();
//        String jwtToken = jwtService.createAccessToken(mockUser, sid);
//
//        // mock BadgeService
//        when(badgeService.getAcquiredBadges(mockUser.getId()))
//                .thenReturn(List.of(
//                        new BadgeResponse(BadgeType.START_HALF, true, 0),
//                        new BadgeResponse(BadgeType.BEGINNER_BATHROOM, true, 0),
//                        new BadgeResponse(BadgeType.EXPERT_BATHROOM, false, 15)
//                ));
//
//        // 요청
//        mockMvc.perform(get("/badges/acquired")
//                .header("Authorization", "Bearer " + jwtToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].type").value(BadgeType.START_HALF.name()))
//                .andExpect(jsonPath("$[0].acquired").value(true))
//                .andExpect(jsonPath("$[0].remainingCount").value(0))
//                .andExpect(jsonPath("$[1].type").value(BadgeType.BEGINNER_BATHROOM.name()))
//                .andExpect(jsonPath("$[1].acquired").value(true))
//                .andExpect(jsonPath("$[1].remainingCount").value(0))
//                .andExpect(jsonPath("$[2].type").value(BadgeType.EXPERT_BATHROOM.name()))
//                .andExpect(jsonPath("$[2].acquired").value(false))
//                .andExpect(jsonPath("$[2].remainingCount").value(15));
//
//    }
//}
