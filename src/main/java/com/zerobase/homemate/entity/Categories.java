package com.zerobase.homemate.entity;


import com.zerobase.homemate.entity.enums.CategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    @Column(name = "target_month", length = 7)
    private String targetMonth;    // "2025-12"

    @Column(nullable = false, length = 50)
    private String title;        // "12월 추천 집안일"

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private LocalDate createdAt;

    // 생성자 (월간 전용)
    public static Categories monthly(String targetMonth, String title, int order) {
        Categories c = new Categories();
        c.type = CategoryType.MONTHLY;
        c.targetMonth = targetMonth;
        c.title = title;
        c.isActive = true;
        c.displayOrder = order;
        c.createdAt = LocalDate.now();
        return c;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
