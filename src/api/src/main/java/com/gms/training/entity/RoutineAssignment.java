package com.gms.training.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "training_routine_assignments")
public class RoutineAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private OffsetDateTime assignedAt;

    protected RoutineAssignment() {
    }

    public RoutineAssignment(Routine routine, Long memberId) {
        this.routine = routine;
        this.memberId = memberId;
    }

    @PrePersist
    void onCreate() {
        this.assignedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Routine getRoutine() { return routine; }
    public Long getMemberId() { return memberId; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
}