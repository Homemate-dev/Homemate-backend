package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Categories;
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


    @Modifying
    @Query("""
    UPDATE CategoryChore c
    SET c.isActive = false
    WHERE c.categoryType = 'MONTHLY'
      AND c.isActive = true
""")
    void deactivateAllMonthly(CategoryType monthly, String string);

    @Modifying
    @Query("""
UPDATE CategoryChore c
SET c.isActive = true
WHERE c.categoryType = 'MONTHLY'
  AND c.yearMonth = :yearMonth
""")
    void activateMonthly(CategoryType monthly, @Param("yearMonth") String yearMonth);

    @Query("""
    SELECT c FROM Categories c
    WHERE c.type = 'MONTHLY'
      AND c.isActive = true
    ORDER BY c.displayOrder ASC
""")
    List<CategoryChore> findByCategoriesAndIsActiveTrue(Categories categories, Pageable pageable);


    long countBySeasonAndCategoryType(Season currentSeason, CategoryType categoryType);

    long countByCategories(Categories selected);
}
