package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.Category;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chore_id", nullable = false)
    private Chore chore;
}
