package com.zerobase.homemate.mission.service;

import com.zerobase.homemate.badge.service.BadgeService;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.MissionProgress;
import com.zerobase.homemate.entity.UserMission;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.MissionType;
import com.zerobase.homemate.entity.enums.RegistrationType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.entity.enums.UserActionType;
import com.zerobase.homemate.mission.dto.MissionDto;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.MissionProgressRepository;
import com.zerobase.homemate.repository.MissionRepository;
import com.zerobase.homemate.repository.UserMissionRepository;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final MissionProgressRepository missionProgressRepository;
    private final BadgeService badgeService;
    private final CategoryChoreRepository categoryChoreRepository;

    public List<MissionDto.Response> getMonthlyMissions(long userId) {

        missionAssignmentService.assignUserMissionForMonth(userId);

        List<Mission> monthlyMissions =
            missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(YearMonth.now());

        if (monthlyMissions.isEmpty()) {
            return List.of();
        }

        List<UserMission> userMissions =
            userMissionRepository.findByUser_IdAndMissionIn(userId, monthlyMissions);

        Map<Long, UserMission> userMissionMap = userMissions.stream()
            .collect(Collectors.toMap(uerMission -> uerMission.getMission().getId(),
                Function.identity()));

        return monthlyMissions.stream()
            .map(mission -> {
                UserMission userMission = userMissionMap.get(mission.getId());
                boolean existsInRecommend = existsInCategoryChores(mission.getTitle());
                return (userMission != null)
                    ? MissionDto.Response.of(mission, userMission,
                    existsInRecommend)
                    : MissionDto.Response.builder()
                        .id(mission.getId())
                        .title(mission.getTitle())
                        .targetCount(mission.getTargetCount())
                        .currentCount(0)
                        .isCompleted(false)
                        .existsInRecommend(existsInRecommend)
                        .build();
            })
            .toList();
    }

    @Transactional
    public List<MissionDto.Response> applyChoreCompletionByStatus(Long userId,
        ChoreInstance choreInstance, boolean isPending) {

        YearMonth nowYm = YearMonth.now();

        List<Mission> monthlyMissions = missionRepository
            .findByActiveYearMonthAndIsActiveTrueAndUserActionTypeInOrderByIdAsc(
                nowYm,
                List.of(
                    UserActionType.COMPLETE_CHORE,
                    UserActionType.COMPLETE_ANY_CHORE,
                    UserActionType.COMPLETE_CHORE_WITH_SPACE
                )
            );

        if (monthlyMissions.isEmpty()) return List.of();

        List<UserMission> userMissions =
            userMissionRepository.findByUser_IdAndMissionIn(userId, monthlyMissions);

        if (userMissions.isEmpty()) return List.of();

        Map<Long, UserMission> userMissionByMissionId = userMissions.stream()
            .collect(Collectors.toMap(um -> um.getMission().getId(),
                Function.identity()));

        Map<Long, MissionProgress> progressByUserMissionId =
            missionProgressRepository.findByUserMissionInAndChoreInstance(
                userMissions, choreInstance)
            .stream()
            .collect(Collectors.toMap(mp ->
                mp.getUserMission().getId(), Function.identity())
            );

        Mission completeCounterMission =
            missionRepository.findByMissionTypeAndUserActionTypeAndActiveYearMonth(
                MissionType.USER_ACTION,
                UserActionType.MISSION_COMPLETED,
                nowYm);

        UserMission counterUserMission = null;
        if (completeCounterMission != null) {
            counterUserMission = userMissionRepository.
                findByUser_IdAndMission(userId, completeCounterMission);
        }

        List<MissionProgress> toSave = new ArrayList<>();
        List<MissionProgress> toDelete = new ArrayList<>();
        List<MissionDto.Response> response = new ArrayList<>();

        for (Mission mission : monthlyMissions) {
            UserMission userMission = userMissionByMissionId.get(mission.getId());
            if (userMission == null) continue;
            if (!qualifies(mission, choreInstance)) continue;

            MissionProgress missionProgress =
                progressByUserMissionId.get(userMission.getId());

            boolean existsInRecommend = existsInCategoryChores(mission.getTitle());

            if (isPending) {
                boolean isAlreadyCompleted = userMission.getIsCompleted();
                applyCompletion(userMission, missionProgress, choreInstance,
                    toSave, counterUserMission);
                if (isAlreadyCompleted) continue;
                if (userMission.getIsCompleted()) {
                    response.add(MissionDto.Response.of(mission, userMission,
                     existsInRecommend));
                }
            } else {
                revertCompletion(userMission, missionProgress, choreInstance,
                    toDelete, counterUserMission);
            }
        }

        return response
            .stream()
            .sorted(Comparator.comparing(MissionDto.Response::getTitle))
            .toList();
    }

    private boolean existsInCategoryChores(String missionTitle) {
        List<CategoryChore> monthlyMissionCategoryChores =
            categoryChoreRepository.findAllByCategory(Category.MISSIONS);

        return monthlyMissionCategoryChores
            .stream()
            .anyMatch(categoryChore -> qualifiesChoreTitle(missionTitle,
                categoryChore.getTitle()));
    }

    private boolean qualifies(Mission mission, ChoreInstance choreInstance) {
        UserActionType type = mission.getUserActionType();
        String missionTitle = mission.getTitle();
        Space missionSpace = mission.getSpace();
        RegistrationType choreRegistrationType =
            choreInstance.getChore().getRegistrationType();
        String choreTitle = choreInstance.getTitleSnapshot();
        Space choreSpace = choreInstance.getChore().getSpace();

        return switch (type) {
            case COMPLETE_ANY_CHORE -> true;
            case COMPLETE_CHORE -> (choreRegistrationType == RegistrationType.CATEGORY
                || choreRegistrationType == RegistrationType.SPACE)
                && qualifiesChoreTitle(missionTitle, choreTitle);
            case COMPLETE_CHORE_WITH_SPACE -> choreRegistrationType == RegistrationType.SPACE
                && missionSpace.equals(choreSpace);
            default -> false;
        };
    }

    public boolean qualifiesChoreTitle(String missionTitle, String choreTitle) {
        if (missionTitle == null || choreTitle == null) return false;


        missionTitle = missionTitle.replaceAll("\\s+", "");
        choreTitle = choreTitle.replaceAll("\\s+", "");


        Set<Character> missionChars = missionTitle.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toSet());

        for (char c : choreTitle.toCharArray()) {
            if (!missionChars.contains(c)) {
                return false;
            }
        }
        return true;
    }

    private void applyCompletion(UserMission userMission,
        MissionProgress missionProgress, ChoreInstance choreInstance,
        List<MissionProgress> toSave, UserMission counterUserMission) {

        if (missionProgress != null || userMission.getIsCompleted()) return;

        boolean missionCompleted = userMission.incrementCount();

        toSave.add(MissionProgress.builder()
            .userMission(userMission)
            .choreInstance(choreInstance)
            .build());

        if (missionCompleted && counterUserMission != null) {
            counterUserMission.incrementCount();
            toSave.add(MissionProgress.builder()
                .userMission(counterUserMission)
                .choreInstance(choreInstance)
                .build());
        }

        missionProgressRepository.saveAll(toSave);

        if(missionCompleted){
            badgeService.evaluateBadgesMission(userMission.getUser());
        }
    }

    private void revertCompletion(UserMission userMission,
        MissionProgress missionProgress, ChoreInstance choreInstance,
        List<MissionProgress> toDelete, UserMission counterUserMission) {

        if (missionProgress == null) return;

        toDelete.add(missionProgress);

        boolean missionCompleted = userMission.decrementCount();

        if (missionCompleted && counterUserMission != null) {
            counterUserMission.decrementCount();

            MissionProgress counterMissionProgress =
                missionProgressRepository.findByUserMissionAndChoreInstance(
                    counterUserMission, choreInstance);

            if (counterMissionProgress != null) {
                toDelete.add(counterMissionProgress);
            }
        }

        missionProgressRepository.deleteAll(toDelete);
    }

    @Transactional
    public List<MissionDto.Response> increaseMissionCountForAction(Long userId,
        UserActionType userActionType) {
        Mission mission =
            missionRepository.findByMissionTypeAndUserActionTypeAndActiveYearMonth(
                MissionType.USER_ACTION, userActionType, YearMonth.now()
            );

        if (mission == null) {
            return List.of();
        }

        UserMission userMission = userMissionRepository.findByUser_IdAndMission(
            userId, mission);

        if (userMission == null) {
            return List.of();
        }

        if (userMission.getIsCompleted()) {
            return List.of();
        }

        userMission.incrementCount();

        missionProgressRepository.save(
            MissionProgress.builder()
                .userMission(userMission)
                .build()
        );

        boolean existsInRecommend = existsInCategoryChores(mission.getTitle());

        return List.of(MissionDto.Response.of(mission, userMission, existsInRecommend));
    }
}
