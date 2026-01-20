package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Categories;
import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.CategoryType;
import com.zerobase.homemate.entity.enums.Season;
import com.zerobase.homemate.entity.enums.SubCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CategoryChoreRepository extends JpaRepository<CategoryChore, Long> {

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
    WHERE c.categoryType = 'SEASON'
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
    WHERE c.categoryType = :categoryType
      AND c.categories.targetMonth = :targetMonth
""")
    void activateMonthly(@Param("categoryType") CategoryType categoryType,
                         @Param("targetMonth") String targetMonth);

    @Query("""
    SELECT cc
    FROM CategoryChore cc
    JOIN cc.categories c
    WHERE c = :categories
      AND cc.categoryType = :categoryType
      AND cc.isActive = true
""")
    List<CategoryChore> findActiveByCategoriesAndCategoryType(
            @Param("categories") Categories categories,
            @Param("categoryType") CategoryType categoryType
    );


    @Query("""
SELECT cc
FROM CategoryChore cc
WHERE cc.categories = :categories
  AND cc.categoryType = :categoryType
  AND cc.subCategory = :subCategory
  AND cc.isActive = true
""")
    List<CategoryChore> findActiveByCategoryTypeAndSubCategory(
            @Param("categories") Categories categories,
            @Param("categoryType") CategoryType categoryType,
            @Param("subCategory") SubCategory subCategory
    );




    long countBySeasonAndCategoryType(Season currentSeason, CategoryType categoryType);
}
