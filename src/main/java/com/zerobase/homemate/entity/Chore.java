package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.RegistrationType;
import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "chore")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Chore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 30)
    @Setter
    private String title;

    @Column(name = "notification_yn", nullable = false)
    @Setter
    private Boolean notificationYn;

    @Column(name = "notification_time")
    @Setter
    private LocalTime notificationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    @Setter
    private RepeatType repeatType;

    @Column(name = "repeat_interval")
    @Setter
    private Integer repeatInterval;

    @Setter
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Setter
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN")
    @Builder.Default
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private Space space;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false)
    @Setter
    private RegistrationType registrationType;

    @OneToMany(mappedBy = "chore", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ChoreInstance> choreInstances = new ArrayList<>();

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
