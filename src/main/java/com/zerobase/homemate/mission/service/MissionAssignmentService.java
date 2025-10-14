package com.zerobase.homemate.mission.service;

import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.repository.MissionRepository;
import com.zerobase.homemate.repository.UserMissionRepository;
import com.zerobase.homemate.repository.UserRepository;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionAssignmentService {

    private static final DateTimeFormatter YM =
        DateTimeFormatter.ofPattern("yyyy-MM");

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final UserMissionRepository userMissionRepository;

    /*
        매월 1일 00:01 에 해당 월의 미션 userMission 테이블에 추가
     */
    @Transactional
    public void assignForMonth(YearMonth yearMonth) {
        List<Mission> missions =
            missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(yearMonth);
        List<Long> userIds =
            userRepository.findIdsByUserStatusAndUserRole(
                UserStatus.ACTIVE, UserRole.USER);

        for (Mission mission : missions) {
            for (Long userId : userIds) {
                userMissionRepository.upsert(
                    userId, mission.getId());
            }
        }
    }

    /*
        호출하는 유저의 미션만 추가
     */
    @Transactional
    public void assignUserMissionForMonth(Long userId) {
        List<Mission> missions =
            missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(YearMonth.now());

        if (missions.isEmpty()) {
            return;
        }

        for (Mission mission : missions) {
            userMissionRepository.upsert(
                userId, mission.getId());
        }
    }
}
