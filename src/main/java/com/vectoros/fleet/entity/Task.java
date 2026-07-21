package com.vectoros.fleet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "task_number", length = 20, nullable = false, unique = true)
    private String taskNumber;

    @NotBlank
    @Size(max = 100)
    @Column(name = "pickup_location", length = 100, nullable = false)
    private String pickupLocation;

    @NotBlank
    @Size(max = 100)
    @Column(name = "dropoff_location", length = 100, nullable = false)
    private String dropoffLocation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_id")
    private Robot assignedRobot;

    @Column(name = "estimated_distance")
    private Double estimatedDistance;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public static Task createNew(String taskNumber, String pickupLocation, String dropoffLocation, Priority priority) {
        Task task = new Task();
        task.taskNumber = taskNumber;
        task.pickupLocation = pickupLocation;
        task.dropoffLocation = dropoffLocation;
        task.priority = priority;
        task.status = TaskStatus.NEW;
        return task;
    }

    public void assignRobot(Robot robot) {
        this.assignedRobot = robot;
        this.status = TaskStatus.ASSIGNED;
    }

    public void updateStatus(TaskStatus newStatus) {
        this.status = newStatus;
        if (newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }
}
