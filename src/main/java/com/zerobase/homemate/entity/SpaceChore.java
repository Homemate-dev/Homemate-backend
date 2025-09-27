package com.zerobase.homemate.entity;

import jakarta.persistence.*;
import com.zerobase.homemate.entity.enums.Frequency;
import lombok.*;

@Entity
@Table(name = "space_chores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SpaceChore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "title_ko", nullable = false, length = 100)
    private String titleKo;

    @Column(name = "default_freq", nullable = false)
    private Frequency defaultFreq;

    @Column(name = "is_active")
    private Boolean isActive;
}
