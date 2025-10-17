package com.zerobase.homemate.recommend.service;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.*;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.zerobase.homemate.util.ChoreDateUtils.calculateEndDate;


@Service
@RequiredArgsConstructor
public class CategoryChoreCreator {

    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;
    private final ChoreInstanceRepository choreInstanceRepository;
    private final ChoreInstanceGenerator choreInstanceGenerator;
    private final CategoryChoreRepository categoryChoreRepository;
    private final SpaceChoreRepository spaceChoreRepository;


    @Transactional
    public ChoreDto.Response createChoreFromCategory(Long userId, Category category, Long categoryChoreId) {
        // 1️⃣ 사용자 유효성 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2️⃣ CategoryChore(템플릿) 조회
        CategoryChore template = categoryChoreRepository.findById(categoryChoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHORE_NOT_FOUND));

        // 3️⃣ ChoreRepository에서 동일한 이름과 사용자로 인해 만들어진 Chore가 있는지 검증
        if(choreRepository.existsByUserIdAndTitle(userId, template.getTitle())){
            throw new CustomException(ErrorCode.CHORE_ALREADY_REGISTERED);
        }

        // 1️⃣ 동일한 집안일 찾기 (title 기준)
        SpaceChore matchedSpaceChore = spaceChoreRepository.findByTitleKo(template.getTitle())
                .orElse(null);

        // 2️⃣ Space 결정: 매칭된 항목이 없으면 ETC
        Space space = (matchedSpaceChore != null) ? matchedSpaceChore.getSpace() : Space.ETC;

        // 3️⃣ Chore 생성
        Chore chore = Chore.builder()
                .user(user)
                .title(template.getTitle())
                .space(space) // 매핑된 Space 또는 ETC
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

        // 4️⃣ 저장
        Chore saved = choreRepository.save(chore);

        // 5️⃣ 반복 인스턴스 생성
        List<ChoreInstance> instances = choreInstanceGenerator.generateInstances(saved);
        choreInstanceRepository.saveAll(instances);

        return ChoreDto.Response.fromEntity(saved);
    }



}
