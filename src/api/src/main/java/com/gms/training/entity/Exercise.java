package com.gms.training.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "training_exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private Integer repetitions;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Exercise() {
    }

    public Exercise(Routine routine, String name, Integer sets, Integer repetitions, Integer displayOrder) {
        this.routine = routine;
        this.name = name;
        this.sets = sets;
        this.repetitions = repetitions;
        this.displayOrder = displayOrder;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Routine getRoutine() { return routine; }
    public String getName() { return name; }
    public Integer getSets() { return sets; }
    public Integer getRepetitions() { return repetitions; }
    public Integer getDisplayOrder() { return displayOrder; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setSets(Integer sets) { this.sets = sets; }
    public void setRepetitions(Integer repetitions) { this.repetitions = repetitions; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}