package com.gms.attendance.repository;

import com.gms.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findFirstByMemberIdOrderByTimestampDesc(Long memberId);

    List<AttendanceRecord> findByMemberIdOrderByTimestampDesc(Long memberId);

    List<AttendanceRecord> findByTimestampBetweenOrderByTimestampDesc(
            OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.timestamp BETWEEN :from AND :to")
    long countByTimestampBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}