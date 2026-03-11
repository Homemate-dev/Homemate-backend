package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.ChoreStatus;
import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chore_instance",
        indexes = {
            @Index(name = "idx_ci_chore_status_dueDate",
                columnList = "chore_id, chore_status, due_date")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChoreInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title_snapshot", nullable = false)
    @Setter
    private String titleSnapshot;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "notification_time")
    @Setter
    private LocalTime notificationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "chore_status", nullable = false)
    @Builder.Default
    private ChoreStatus choreStatus = ChoreStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chore_id", updatable = false)
    private Chore chore;

    public void cancelChore() {
        this.choreStatus = ChoreStatus.CANCELLED;
    }

    public void completeChore() {
        this.choreStatus = ChoreStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancelCompleteChore() {
        this.choreStatus = ChoreStatus.PENDING;
        this.completedAt = null;
    }

    public void softDelete() {
        if (this.choreStatus == ChoreStatus.PENDING ||
            this.choreStatus == ChoreStatus.COMPLETED ||
            this.choreStatus == ChoreStatus.CANCELLED) {
            this.choreStatus = ChoreStatus.DELETED;
            this.deletedAt = LocalDateTime.now();
        }
    }
}
