package com.zerobase.homemate.mission.service;

import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.UserMission;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.repository.MissionRepository;
import com.zerobase.homemate.repository.UserMissionRepository;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final MissionAssignmentService missionAssignmentService;

    public List<MissionDto.Response> getMonthlyMissions(long userId) {

        missionAssignmentService.assignUserMissionForMonth(userId);

        List<Mission> monthlyMissions =
            missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(YearMonth.now());

        if (monthlyMissions.isEmpty()) {
            return List.of();
        }

        List<Long> missionIds = monthlyMissions.stream()
            .map(Mission::getId)
            .toList();

        List<UserMission> userMissions =
            userMissionRepository.findByUser_IdAndMission_IdIn(userId, missionIds);

        Map<Long, UserMission> userMissionMap = userMissions.stream()
            .collect(Collectors.toMap(uerMission -> uerMission.getMission().getId(),
                Function.identity()));

        return monthlyMissions.stream()
            .map(mission -> {
                UserMission userMission = userMissionMap.get(mission.getId());
                return (userMission != null)
                    ? MissionDto.Response.of(mission, userMission)
                    : MissionDto.Response.builder()
                        .id(mission.getId())
                        .title(mission.getTitle())
                        .targetCount(mission.getTargetCount())
                        .currentCount(0)
                        .isCompleted(false)
                        .build();
            })
            .toList();
    }
}
