package com.gms.attendance.controller;

import com.gms.attendance.entity.AttendanceRecord;
import com.gms.attendance.service.AttendanceFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceFacade attendanceFacade;

    public AttendanceController(AttendanceFacade attendanceFacade) {
        this.attendanceFacade = attendanceFacade;
    }

    @PostMapping("/entries")
    public ResponseEntity<?> registerEntry(@RequestBody Map<String, String> body) {
        AttendanceRecord record = attendanceFacade.registerEntry(body.get("accessCode"));
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @PostMapping("/exits")
    public ResponseEntity<?> registerExit(@RequestBody Map<String, String> body) {
        AttendanceRecord record = attendanceFacade.registerExit(body.get("accessCode"));
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<List<AttendanceRecord>> list(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to) {
        if (memberId != null) {
            return ResponseEntity.ok(attendanceFacade.findByMember(memberId));
        }
        if (from != null && to != null) {
            return ResponseEntity.ok(attendanceFacade.findByDateRange(from, to));
        }
        return ResponseEntity.ok(attendanceFacade.findByDateRange(
                OffsetDateTime.now().minusDays(30), OffsetDateTime.now()));
    }
}