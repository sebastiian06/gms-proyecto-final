package com.gms.attendance.service;

import com.gms.attendance.entity.AttendanceRecord;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class AttendanceFacade {

    private final AttendanceService attendanceService;

    public AttendanceFacade(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public AttendanceRecord registerEntry(String accessCode) {
        return attendanceService.registerEntry(accessCode);
    }

    public AttendanceRecord registerExit(String accessCode) {
        return attendanceService.registerExit(accessCode);
    }

    public List<AttendanceRecord> findByMember(Long memberId) {
        return attendanceService.findByMember(memberId);
    }

    public List<AttendanceRecord> findByDateRange(OffsetDateTime from, OffsetDateTime to) {
        return attendanceService.findByDateRange(from, to);
    }

    public long countThisMonth() {
        return attendanceService.countThisMonth();
    }
}