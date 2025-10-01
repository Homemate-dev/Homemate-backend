package com.zerobase.homemate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "category_chores",
uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "chore_id"})
})
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

    public static CategoryChore of(Category category, Chore chore) {
        return CategoryChore
                .builder()
                .id(category.getId())
                .chore(chore)
                .build();
    }
}
