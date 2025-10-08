package com.zerobase.homemate.recommend;


import com.zerobase.homemate.recommend.dto.ChoreResponse;
import com.zerobase.homemate.recommend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Transactional
@Rollback
class CategoryControllerTest {


    @Autowired
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testGetChoresByCategory() {
        // given
        Long categoryId = 1L; // DB에 넣어둔 category_id 값

        // when
        List<ChoreResponse> chores = categoryService.getChoresByCategory(categoryId);

        // then
        assertThat(chores).isNotNull();
        assertThat(chores.size()).isLessThanOrEqualTo(4); // 페이지 사이즈 4개로 제한했으니까
        chores.forEach(chore -> {
            System.out.println("조회된 Chore: " + chore.title());
        });
    }




}
