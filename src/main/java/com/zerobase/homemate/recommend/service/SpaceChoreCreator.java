package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.ChoreInstanceRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.zerobase.homemate.util.ChoreDateUtils.calculateEndDate;

@Service
@RequiredArgsConstructor
public class SpaceChoreCreator {

    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final ChoreInstanceGenerator choreInstanceGenerator;
    private final SpaceChoreRepository spaceChoreRepository;

    @Transactional
    public ChoreDto.Response createChoreFromSpace(Long userId, Space space, Long spaceChoreId){
        // 1. 사용자 유효성 검증
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. SpaceChore 조회
        SpaceChore template = spaceChoreRepository.findById(spaceChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        // 3. 동일한 사용자가 이미 등록한 집안일인지 검증
        if(choreRepository.existsByUserIdAndTitle(userId, template.getTitleKo())){
            throw new CustomException(ErrorCode.CHORE_ALREADY_REGISTERED);
        }

        // 4. Chore 생성
        Chore chore = Chore.builder()
                .user(user)
                .title(template.getTitleKo())
                .space(template.getSpace())
                .repeatType(template.getRepeatType())
                .repeatInterval(template.getRepeatInterval())
                .startDate(LocalDate.now())
                .endDate(calculateEndDate(
                        LocalDate.now(),
                        template.getRepeatType(),
                        template.getRepeatInterval()
                ))
                .notificationYn(false)
                .isDeleted(false)
                .build();

        Chore saved = choreRepository.save(chore);

        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(saved);
        choreInstanceRepository.saveAll(instances);

        return ChoreDto.Response.fromEntity(saved);
    }

}
