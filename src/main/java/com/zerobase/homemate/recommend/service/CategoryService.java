package com.zerobase.homemate.recommend.service;




import com.zerobase.homemate.entity.Category;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.CategoryRepository;
import com.zerobase.homemate.repository.ChoreRepository;
import com.zerobase.homemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final ChoreRepository choreRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryChoreRepository categoryChoreRepository;
    private static final int DEFAULT_PAGE_SIZE = 4;

    public List<ChoreResponse> getChoresByCategory(Long categoryId) {
        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        // 집안일 조회 (없으면 빈 리스트 반환)
        return choreRepository.findByCategoryChores_Category_Id(
                        categoryId,
                        PageRequest.of(0, DEFAULT_PAGE_SIZE)
                )
                .stream()
                .map(ChoreResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void createDummyData() {

        User testuser = User.builder()
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
        userRepository.save(testuser);
        // 1. 카테고리 생성
        Category winter = Category
                .builder()
                .nameKo("겨울철 대청소")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .description("겨울철에 진행하는 청소 모음")
                .isActive(true)
                .build();
        categoryRepository.save(winter);

        Category dailyTen = Category
                .builder()
                .nameKo("하루 10분 청소하기")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        categoryRepository.save(dailyTen);

        // 2. 집안일 생성
        Chore vacuum = Chore.builder()
                .userId(testuser.getId())
                .title("청소기 돌리기")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .notificationYn(true)
                .notificationTime(LocalTime.now().plusHours(3))
                .repeatType(Chore.RepeatType.WEEKLY)
                .build();
        choreRepository.save(vacuum);

        Chore dishes = Chore.builder()
                .userId(testuser.getId())
                .title("설거지 하기")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .notificationYn(true)
                .notificationTime(LocalTime.now().plusHours(3))
                .repeatType(Chore.RepeatType.WEEKLY)
                .build();
        choreRepository.save(dishes);

        Chore laundry = Chore.builder()
                .userId(testuser.getId())
                .title("빨래하기")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .notificationYn(true)
                .notificationTime(LocalTime.now().plusMinutes(30))
                .repeatType(Chore.RepeatType.WEEKLY)
                .build();
        choreRepository.save(laundry);

        // 3. 카테고리-집안일 매핑
        categoryChoreRepository.save(
                CategoryChore.builder()
                        .category(dailyTen)
                        .chore(vacuum)
                        .build()
        );

        categoryChoreRepository.save(
                CategoryChore.builder()
                        .category(dailyTen)
                        .chore(dishes)
                        .build()
        );

        categoryChoreRepository.save(
                CategoryChore.builder()
                        .category(winter)
                        .chore(laundry)
                        .build()
        );

    }
}
