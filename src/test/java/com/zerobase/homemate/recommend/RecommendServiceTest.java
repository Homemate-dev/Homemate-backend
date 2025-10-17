package com.zerobase.homemate.recommend;

import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.service.RecommendService;
import com.zerobase.homemate.repository.SpaceChoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RecommendServiceTest {

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @InjectMocks
    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRandomChores_shouldReturnThreeChores(){
        // given

        SpaceChore sc1 = SpaceChore.builder()
                .titleKo("주방 싱크대 비우기")
                .space(Space.KITCHEN)
                .code("주방")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        SpaceChore sc2 = SpaceChore.builder()
                .titleKo("기상 후 침구 정리하기")
                .space(Space.BEDROOM)
                .code("침실")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        SpaceChore sc3 = SpaceChore.builder()
                .titleKo("신발정리")
                .space(Space.PORCH)
                .code("현관")
                .repeatType(RepeatType.WEEKLY)
                .repeatInterval(1)
                .build();

        SpaceChore sc4 = SpaceChore.builder()
                .titleKo("환기하기")
                .space(Space.ETC)
                .code("기타")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(1)
                .build();

        spaceChoreRepository.save(sc1);
        spaceChoreRepository.save(sc2);
        spaceChoreRepository.save(sc3);
        spaceChoreRepository.save(sc4);

    }
}
