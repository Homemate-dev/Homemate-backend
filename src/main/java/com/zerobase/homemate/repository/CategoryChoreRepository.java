package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CategoryChoreRepository extends JpaRepository<CategoryChore, Long> {

    @Query("""
    SELECT c
    from CategoryChore c
    WHERE c.category = :category
    ORDER BY function('RAND')
""")
    List<CategoryChore> findByCategory(@Param("category") Category category, Pageable pageable);

    Optional<CategoryChore> findByTitle(String titleKo);

    Long countByCategory(Category category);

    boolean existsByChoreAndCategory(Chore chore, Category targetCategory);
}
