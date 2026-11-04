package com.gms.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "billing_memberships")
public class Membership {

    public enum Status {
        ACTIVE, EXPIRED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "locked_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal lockedPrice;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Membership() {
    }

    public Membership(Long memberId, Long planId, BigDecimal lockedPrice,
                      LocalDate startDate, LocalDate endDate) {
        this.memberId = memberId;
        this.planId = planId;
        this.lockedPrice = lockedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = Status.ACTIVE;
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

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void expire() {
        this.status = Status.EXPIRED;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getPlanId() { return planId; }
    public BigDecimal getLockedPrice() { return lockedPrice; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Status getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}