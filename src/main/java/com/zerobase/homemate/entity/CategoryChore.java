package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.RepeatType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;
}
