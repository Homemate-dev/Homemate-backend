package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.MonthlyRecommendResponse;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.repository.CategoriesRepository;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendServiceTest {

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    @InjectMocks
    private RecommendService recommendService;

    @Test
    void getRandomChores_shouldReturnThreeChores(){
        // given - 익명 클래스 방식으로 두 필드 구현
        SpaceChoreResponse sc1 = new SpaceChoreResponse() {
            public Long getId() { return 1L; }
            public String getTitleKo() { return "주방 싱크대 청소하기"; }
            public Space getSpace() { return Space.KITCHEN; }
        };
        SpaceChoreResponse sc2 = new SpaceChoreResponse() {
            public Long getId() { return 2L; }
            public String getTitleKo() { return "신발정리"; }
            public Space getSpace() { return Space.PORCH; }
        };
        SpaceChoreResponse sc3 = new SpaceChoreResponse() {
            public Long getId() { return 3L; }
            public String getTitleKo() { return "환기하기"; }
            public Space getSpace() { return Space.ETC; }
        };

        SpaceChoreResponse sc4 = new SpaceChoreResponse() {
            public Long getId() { return 4L; }
            public String getTitleKo() { return "에어컨 필터 청소하기"; }
            public Space getSpace() { return Space.ETC; }
        };

        when(spaceChoreRepository.findRandomChores()).thenReturn(List.of(sc1, sc2, sc4));

        List<SpaceChoreResponse> responses = recommendService.getRandomChores();

        assertEquals(3, responses.size());
        assertEquals("신발정리", responses.get(1).getTitleKo());
    }

    @Test
    @DisplayName("이번 달 활성화된 월간 카테고리를 조회한다")
    void getMonthlyRecommends_success(){
        // given
        String thisMonth = YearMonth
                .now(ZoneId.of("Asia/Seoul"))
                .toString();
        Categories category1 = Categories.monthly(thisMonth, "2월 1째 카테고리", 1);
        Categories category2 = Categories.monthly(thisMonth, "2월 2째 카테고리", 2);


        when(categoriesRepository.findActiveMonthlyByTargetMonth(anyString()))
                .thenReturn(List.of(category1, category2));

        // when
        List<MonthlyRecommendResponse> result = recommendService.getMonthlyCategories();

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(categoriesRepository)
                .findActiveMonthlyByTargetMonth(captor.capture());

        assertThat(captor.getValue())
                .isEqualTo(YearMonth.now(ZoneId.of("Asia/Seoul")).toString());

        String expectedMonth = YearMonth
                .now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        assertThat(captor.getValue()).isEqualTo(expectedMonth);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).categoryName()).isEqualTo("2월 1째 카테고리");
    }
}
