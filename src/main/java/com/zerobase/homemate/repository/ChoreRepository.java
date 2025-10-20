package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {


    boolean existsByUserIdAndTitle(Long userId, String title);

    Long countByUserAndSpaceAndIsCompletedTrue(User user, Space space);

    Long countByUserAndCategoryAndIsCompletedTrue(User user, Category category);

    Long countByUserAndTitleAndIsCompletedTrue(User user, String title);
}
