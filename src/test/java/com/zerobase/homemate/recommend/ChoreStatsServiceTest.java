package com.zerobase.homemate.recommend;


import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.Mission;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.entity.enums.MissionType;
import com.zerobase.homemate.entity.enums.Season;
import com.zerobase.homemate.exception.CustomException;
import com.zerobase.homemate.exception.ErrorCode;
import com.zerobase.homemate.recommend.dto.TopItemDto;
import com.zerobase.homemate.recommend.service.stats.ChoreStatsService;
import com.zerobase.homemate.recommend.service.stats.RedisChoreStatsService;
import com.zerobase.homemate.repository.CategoriesRepository;
import com.zerobase.homemate.repository.CategoryChoreRepository;
import com.zerobase.homemate.repository.MissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChoreStatsServiceTest {

    @InjectMocks
    private ChoreStatsService choreStatsService;

    @Mock
    private RedisChoreStatsService redisChoreStatsService;

    @Mock
    private CategoryChoreRepository categoryChoreRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    @Test
    void getRandomMonthlyTop_noMonthlyCategory_throwException() {
        when(categoriesRepository.findActiveMonthlyByYearMonth(any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> choreStatsService.getTopCategories(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACTIVE_CATEGORY_NOT_FOUND);
    }


    @Test
    void getTopCategories_success(){
        // given
        YearMonth yearMonth = YearMonth.now();

        Mission mission = mock(Mission.class);

        when(mission.getMissionType()).thenReturn(MissionType.CHORE);

        when(missionRepository.findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(yearMonth))
                .thenReturn(List.of(mission, mission));

        when(categoryChoreRepository.countBySeasonAndCategoryType(
                any(Season.class), eq(CategoryType.SEASON)
        )).thenReturn(10L);

        Categories monthly = mock(Categories.class);

        when(monthly.getTitle()).thenReturn("1월 추천 집안일");

        when(categoriesRepository.findActiveMonthlyByYearMonth(yearMonth.toString()))
                .thenReturn(List.of(monthly));

        when(categoryChoreRepository.countByCategories(monthly)).thenReturn(7L);

        // when
        List<TopItemDto> result = choreStatsService.getTopCategories(1L);

        // then
        assertThat(result).hasSize(3);

        assertThat(result.get(0).name())
                .isEqualTo(Category.MISSIONS.getCategoryName());

        assertThat(result.get(1).count())
                .isEqualTo(10L);

        // 월간 (Category enum 없음)
        assertThat(result.get(2).name())
                .isEqualTo("1월 추천 집안일");
        assertThat(result.get(2).category()).isNull();
        assertThat(result.get(2).count()).isEqualTo(7L);

    }


}
