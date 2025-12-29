package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.entity.enums.Season;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CategoryChoreRepository extends JpaRepository<CategoryChore, Long> {

    @Query("""
    SELECT c
    from CategoryChore c
    WHERE c.category = :category
    ORDER BY function('RAND')
""")
    List<CategoryChore> findByCategory(@Param("category") Category category, Pageable pageable);

    List<CategoryChore> findAllByTitle(String titleKo);

    Long countByCategory(Category category);

    List<CategoryChore> findAllByCategory(Category category);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CategoryChore c WHERE c.category = :category")
    void deleteByCategory(@Param("category") Category category);

    @Query("""
    SELECT c
    FROM CategoryChore c
    WHERE c.categoryType = 'FIXED'
      AND c.category = :category
      AND c.isActive = true
    ORDER BY c.id DESC
""")
    List<CategoryChore> findActiveFixedByCategory(
            @Param("category") Category category,
            Pageable pageable
    );



    @Query("""
    SELECT c
    FROM CategoryChore c
    WHERE c.categoryType = 'SEASONAL'
      AND c.season = :season
      AND c.isActive = true
""")
    List<CategoryChore> findActiveSeasonalBySeason(
            @Param("season") Season season,
            Pageable pageable
    );


    @Query("""
    SELECT c
    FROM CategoryChore c
    WHERE c.categoryType = 'MONTHLY'
      AND c.yearMonth = :yearMonth
      AND c.isActive = true
""")
    List<CategoryChore> findActiveMonthlyByYearMonth(
            @Param("yearMonth") String yearMonth,
            Pageable pageable
    );

    @Modifying
    @Query("""
    UPDATE CategoryChore c
    SET c.isActive = false
    WHERE c.categoryType = 'MONTHLY'
      AND c.isActive = true
""")
    void deactivateAllMonthly();

    @Modifying
    @Query("""
UPDATE CategoryChore c
SET c.isActive = true
WHERE c.categoryType = 'MONTHLY'
  AND c.yearMonth = :yearMonth
""")
    void activateMonthly(@Param("yearMonth") String yearMonth);

    @Query("""
    SELECT COUNT(c) > 0
    FROM CategoryChore c
    WHERE c.categoryType = 'MONTHLY'
      AND c.yearMonth = :yearMonth
      AND c.isActive = true
""")
    boolean existsActiveMonthly(@Param("yearMonth") String yearMonth);


}
