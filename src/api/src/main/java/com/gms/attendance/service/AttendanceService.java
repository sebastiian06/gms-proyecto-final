package com.gms.attendance.service;

import com.gms.attendance.entity.AttendanceRecord;
import com.gms.attendance.repository.AttendanceRepository;
import com.gms.identity.entity.User;
import com.gms.identity.service.AccessCodeService;
import com.gms.members.dto.MemberDto;
import com.gms.members.service.MembersFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AccessCodeService accessCodeService;
    private final MembersFacade membersFacade;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             AccessCodeService accessCodeService,
                             MembersFacade membersFacade) {
        this.attendanceRepository = attendanceRepository;
        this.accessCodeService = accessCodeService;
        this.membersFacade = membersFacade;
    }

    @Transactional
    public AttendanceRecord registerEntry(String accessCode) {
        User user = accessCodeService.findByCode(accessCode).getUser();
        MemberDto member = membersFacade.findByUserId(user.getId());

        if (!member.active()) {
            throw new MemberInactiveException();
        }

        attendanceRepository.findFirstByMemberIdOrderByTimestampDesc(member.id())
                .ifPresent(last -> {
                    if (last.getDirection() == AttendanceRecord.Direction.IN) {
                        throw new AlreadyCheckedInException();
                    }
                });

        AttendanceRecord record = new AttendanceRecord(member.id(), AttendanceRecord.Direction.IN);
        return attendanceRepository.save(record);
    }

    @Transactional
    public AttendanceRecord registerExit(String accessCode) {
        User user = accessCodeService.findByCode(accessCode).getUser();
        MemberDto member = membersFacade.findByUserId(user.getId());

        AttendanceRecord last = attendanceRepository
                .findFirstByMemberIdOrderByTimestampDesc(member.id())
                .orElseThrow(NotCheckedInException::new);

        if (last.getDirection() != AttendanceRecord.Direction.IN) {
            throw new NotCheckedInException();
        }

        AttendanceRecord record = new AttendanceRecord(member.id(), AttendanceRecord.Direction.OUT);
        return attendanceRepository.save(record);
    }

    public List<AttendanceRecord> findByMember(Long memberId) {
        return attendanceRepository.findByMemberIdOrderByTimestampDesc(memberId);
    }

    public List<AttendanceRecord> findByDateRange(OffsetDateTime from, OffsetDateTime to) {
        return attendanceRepository.findByTimestampBetweenOrderByTimestampDesc(from, to);
    }

    public long countThisMonth() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime start = now.withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMonths(1);
        return attendanceRepository.countByTimestampBetween(start, end);
    }

    public static class MemberInactiveException extends RuntimeException {
        public MemberInactiveException() { super("Member is inactive"); }
    }

    public static class AlreadyCheckedInException extends RuntimeException {
        public AlreadyCheckedInException() { super("Member is already checked in. Please check out first."); }
    }

    public static class NotCheckedInException extends RuntimeException {
        public NotCheckedInException() { super("Member is not checked in."); }
    }
}