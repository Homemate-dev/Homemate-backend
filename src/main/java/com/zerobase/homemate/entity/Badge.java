package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.BadgeType;
import com.zerobase.homemate.entity.enums.Space;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "space")
    private Space space;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private BadgeType badgeType;

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt;

    @Column(name = "badge_image_url")
    private String badgeImageUrl;

    @Builder
    public Badge(User user, BadgeType badgeType) {
        this.user = user;
        this.badgeType = badgeType;
        this.acquiredAt = LocalDateTime.now();
        this.space = badgeType.getSpace();
    }
}
