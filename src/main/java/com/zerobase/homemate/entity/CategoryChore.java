package com.zerobase.homemate.entity;


import jakarta.persistence.*;
import lombok.*;
import com.zerobase.homemate.entity.enums.Frequency;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "title_ko", nullable = false, length = 100)
    private String titleKo;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_freq", nullable = false)
    private Frequency defaultFreq; // 안쪽에 있는 RepeatType이랑 똑같은 기능을 해줄 것으로 보인다.

    @Column(name = "is_active", nullable = false)
    private boolean  isActive;
}
