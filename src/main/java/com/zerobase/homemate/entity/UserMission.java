package com.zerobase.homemate.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_mission",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_user_mission",
            columnNames = {"user_id", "mission_id"})
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "current_count", nullable = false)
    @Builder.Default
    private Integer currentCount = 0;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "userMission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionProgress> progressList = new ArrayList<>();

    public boolean isAlreadyCompleted() {
        return Boolean.TRUE.equals(this.isCompleted);
    }

    public boolean incrementCount() {
        Integer target = this.mission.getTargetCount();

        if (target == null || this.currentCount < target) {
            this.currentCount += 1;
        }

        if (target != null && this.currentCount >= target) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public boolean decrementCount() {
        int next = Math.max(0, this.currentCount - 1);
        this.currentCount = next;

        Integer target = this.mission.getTargetCount();
        if (target != null && next < target) {
            this.isCompleted = false;
            this.completedAt = null;
            return true;
        }
        return false;
    }
}
