package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "category_chores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CategoryChore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 30)
    @Setter
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType;

    @Column(name = "repeat_interval")
    private Integer repeatInterval;

    // 기존 FIXED 카테고리
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    // 신규 계절별 카테고리
    @Enumerated(EnumType.STRING)
    private Season season;

    // 카테고리의 종류 표기
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    // 월간 추가되는 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categories_id")
    private Categories categories;

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory;



    @Column(name = "is_active")
    private boolean isActive;

}
