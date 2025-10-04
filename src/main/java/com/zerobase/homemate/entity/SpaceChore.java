    package com.zerobase.homemate.entity;

    import jakarta.persistence.*;
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
        private Chore.RepeatType defaultFreq;

        @Column(name = "is_active")
        private Boolean isActive;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "space_id")
        private Space space;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "chore_id")
        private Chore chore;


    }
