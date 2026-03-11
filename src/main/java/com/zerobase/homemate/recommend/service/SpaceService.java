package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.UserNotificationSetting;
import com.zerobase.homemate.entity.enums.RegistrationType;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ClassifyChoreResponse;
import com.zerobase.homemate.recommend.dto.SpaceChoreDto;
import com.zerobase.homemate.recommend.dto.SpaceResponse;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.UserNotificationSettingRepository;
import com.zerobase.homemate.util.ChoreDateUtils;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceChoreRepository spaceChoreRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final ChoreRepository choreRepository;

    private static final Map<RepeatType, Integer> REPEAT_PRIORITY = Map.of(
            RepeatType.DAILY, 1,
            RepeatType.WEEKLY, 2,
            RepeatType.MONTHLY, 3,
            RepeatType.YEARLY, 4,
            RepeatType.NONE, 5
    );



    public List<ClassifyChoreResponse> getSpaceChores(Space space, Long userId){

        List<SpaceChore> randomChores = (space == null)
                ? spaceChoreRepository.findAll()
                : spaceChoreRepository.findBySpace(space);
        if (randomChores.isEmpty()) {
            throw new CustomException(ErrorCode.CHORE_NOT_FOUND);
        }

        Set<String> userChores = new HashSet<>(choreRepository.findActiveTitlesByUserIdAndRegistrationTypes(userId, List.of(RegistrationType.CATEGORY, RegistrationType.SPACE)));

        return withDuplicateFlagsSpace(randomChores.stream().toList(), userChores);
    }

    public List<SpaceResponse> getAllSpaces() {
        return Arrays.stream(Space.values())
                .map(space -> new SpaceResponse(space.name(), space))
                .toList();
    }

    public SpaceChoreDto.Response getSpaceChore(
        Long userId, Long spaceChoreId) {
        SpaceChore spaceChore = spaceChoreRepository.findById(spaceChoreId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));
        UserNotificationSetting userNotificationSetting =
            userNotificationSettingRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        LocalDate endDate = ChoreDateUtils.calculateEndDate(LocalDate.now(),
            spaceChore.getRepeatType(),
            spaceChore.getRepeatInterval());
        return SpaceChoreDto.Response.of(spaceChore, userNotificationSetting, endDate);
    }

    // SubMethod - Space for Duplicate
    private List<ClassifyChoreResponse> withDuplicateFlagsSpace(
            List<SpaceChore> chores,
            Set<String> userChoreTitles
    ){
        return chores.stream()
                .sorted(Comparator.comparingInt(
                        c -> REPEAT_PRIORITY.get(c.getRepeatType())
                ))
                .map(c -> ClassifyChoreResponse.fromSpace(
                        c,
                        userChoreTitles.contains(c.getTitleKo())
                ))
                .toList();
    }
}
