package com.zerobase.homemate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monthly_category_trends")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyCategoryTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id",  nullable = false)
    private Category category;

    @Column(nullable = false, length = 6)
    private String yyyymm;

    @Column(nullable = false)
    private Integer registerCount;

    @Builder
    public MonthlyCategoryTrend(String yyyymm, Category category, Integer registerCount) {
        this.yyyymm = yyyymm;
        this.category = category;
        this.registerCount = registerCount;
    }
}
