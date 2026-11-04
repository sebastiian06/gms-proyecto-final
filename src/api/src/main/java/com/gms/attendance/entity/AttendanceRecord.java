package com.gms.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "attendance_attendance_records")
public class AttendanceRecord {

    public enum Direction {
        IN, OUT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Direction direction;

    protected AttendanceRecord() {
    }

    public AttendanceRecord(Long memberId, Direction direction) {
        this.memberId = memberId;
        this.direction = direction;
    }

    @PrePersist
    void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public Direction getDirection() { return direction; }
}