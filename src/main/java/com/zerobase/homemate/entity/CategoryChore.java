package com.zerobase.homemate.entity;


import com.zerobase.homemate.entity.enums.Category;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chore_id", nullable = false)
    private Chore chore;
}
