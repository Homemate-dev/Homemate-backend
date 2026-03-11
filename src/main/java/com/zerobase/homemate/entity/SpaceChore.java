    package com.zerobase.homemate.entity;

    import com.zerobase.homemate.entity.enums.RepeatType;
    import com.zerobase.homemate.entity.enums.Space;
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

        @Enumerated(EnumType.STRING)
        @Column(name = "repeat_type", nullable = false)
        private RepeatType repeatType;

        @Column(name = "repeat_interval")
        private Integer repeatInterval;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Space space;

    }
