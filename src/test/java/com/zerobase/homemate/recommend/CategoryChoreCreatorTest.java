package com.zerobase.homemate.recommend;

import com.zerobase.homemate.chore.dto.ChoreDto;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.service.CategoryChoreCreator;
import com.zerobase.homemate.repository.*;
import com.zerobase.homemate.util.ChoreInstanceGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryChoreCreatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private CategoryChoreRepository categoryChoreRepository;

    @Mock
    private ChoreInstanceRepository choreInstanceRepository;

    @Mock
    private SpaceChoreRepository spaceChoreRepository;

    @Mock
    private ChoreInstanceGenerator choreInstanceGenerator;

    @InjectMocks
    private CategoryChoreCreator categoryChoreCreator;

    @Test
    void createChoreFromCategory_shouldCreateChoreWithMatchedSpace(){
        // given

        Long userId = 1L;
        Long categoryChoreId = 1L;

        User user = User.builder().id(userId).build();

        CategoryChore categoryChore = CategoryChore.builder()
                .category(Category.WINTER)
                .title("청소하기")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();
        categoryChoreRepository.save(categoryChore);

        SpaceChore spaceChore = SpaceChore.builder()
                .space(Space.KITCHEN)
                .titleKo("청소하기")
                .code("주방")
                .repeatType(RepeatType.DAILY)
                .repeatInterval(3)
                .build();
        spaceChoreRepository.save(spaceChore);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryChoreRepository.findById(categoryChoreId)).thenReturn(Optional.of(categoryChore));
        when(spaceChoreRepository.findByTitleKo(spaceChore.getTitleKo())).thenReturn(Optional.of(spaceChore));
        when(choreRepository.save(any(Chore.class))).thenAnswer(inv -> inv.getArguments()[0]);
        when(choreInstanceGenerator.generateInstances(any(Chore.class))).thenReturn(List.of());

        // when
        ChoreDto.Response response = categoryChoreCreator.createChoreFromCategory(userId, Category.WINTER, categoryChoreId);

        // then
        assertEquals("청소하기", response.getTitle());
        assertEquals(Space.KITCHEN, response.getSpace());
        verify(choreRepository).save(any(Chore.class));
        verify(choreInstanceRepository).saveAll(anyList());
    }
}
