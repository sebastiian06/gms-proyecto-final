package com.gms.platform.errors;

import com.gms.attendance.service.AttendanceService;
import com.gms.billing.service.MembershipService;
import com.gms.billing.service.PaymentService;
import com.gms.identity.service.AccessCodeService;
import com.gms.identity.service.AuthService;
import com.gms.identity.service.UserService;
import com.gms.identity.service.UserService.DuplicateUsernameException;
import com.gms.members.service.MemberService;
import com.gms.plans.service.PlanService;
import com.gms.training.service.ExerciseService;
import com.gms.training.service.RoutineService;
import com.gms.training.service.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/duplicate-username"));
        problem.setTitle("Duplicate username");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://gms.example.com/errors/validation-error"));
        problem.setTitle("Validation error");
        problem.setDetail("One or more fields failed validation");
        return problem;
    }

    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(AuthService.InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setType(URI.create("https://gms.example.com/errors/invalid-credentials"));
        problem.setTitle("Invalid credentials");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MemberService.DuplicateDocumentNumberException.class)
    public ProblemDetail handleDuplicateDocumentNumber(
            MemberService.DuplicateDocumentNumberException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/duplicate-document-number"));
        problem.setTitle("Duplicate document number");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MemberService.MemberNotFoundException.class)
    public ProblemDetail handleMemberNotFound(MemberService.MemberNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://gms.example.com/errors/member-not-found"));
        problem.setTitle("Member not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MemberService.AlreadyInactiveException.class)
    public ProblemDetail handleAlreadyInactive(MemberService.AlreadyInactiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/member-already-inactive"));
        problem.setTitle("Member already inactive");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(PlanService.DuplicatePlanNameException.class)
    public ProblemDetail handleDuplicatePlanName(PlanService.DuplicatePlanNameException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/duplicate-plan-name"));
        problem.setTitle("Duplicate plan name");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(PlanService.PlanNotFoundException.class)
    public ProblemDetail handlePlanNotFound(PlanService.PlanNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://gms.example.com/errors/plan-not-found"));
        problem.setTitle("Plan not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(PlanService.AlreadyInactiveException.class)
    public ProblemDetail handlePlanAlreadyInactive(PlanService.AlreadyInactiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/plan-already-inactive"));
        problem.setTitle("Plan already inactive");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MembershipService.ActiveMembershipAlreadyExistsException.class)
    public ProblemDetail handleActiveMembershipExists(
            MembershipService.ActiveMembershipAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/active-membership-exists"));
        problem.setTitle("Active membership already exists");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({MembershipService.MemberInactiveException.class,
                    MembershipService.PlanInactiveException.class})
    public ProblemDetail handleInactiveMemberOrPlan(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/inactive"));
        problem.setTitle("Inactive");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MembershipService.MembershipNotFoundException.class)
    public ProblemDetail handleMembershipNotFound(MembershipService.MembershipNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://gms.example.com/errors/membership-not-found"));
        problem.setTitle("Membership not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MembershipService.MembershipNotActiveException.class)
    public ProblemDetail handleMembershipNotActive(
            MembershipService.MembershipNotActiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/membership-not-active"));
        problem.setTitle("Membership not active");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(PaymentService.PaymentExceedsBalanceException.class)
    public ProblemDetail handlePaymentExceedsBalance(
            PaymentService.PaymentExceedsBalanceException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/payment-exceeds-balance"));
        problem.setTitle("Payment exceeds membership balance");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(PlanService.AlreadyActiveException.class)
    public ProblemDetail handlePlanAlreadyActive(PlanService.AlreadyActiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/plan-already-active"));
        problem.setTitle("Plan already active");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MemberService.MemberAlreadyActiveException.class)
    public ProblemDetail handleMemberAlreadyActive(MemberService.MemberAlreadyActiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/member-already-active"));
        problem.setTitle("Member already active");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({
            TrainerService.TrainerNotFoundException.class,
            RoutineService.RoutineNotFoundException.class,
            RoutineService.NoRoutineAssignedException.class,
            ExerciseService.ExerciseNotFoundException.class
    })
    public ProblemDetail handleTrainingNotFound(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://gms.example.com/errors/not-found"));
        problem.setTitle("Not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(UserService.UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserService.UserNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://gms.example.com/errors/user-not-found"));
        problem.setTitle("User not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({
            UserService.UserAlreadyActiveException.class,
            UserService.UserAlreadyInactiveException.class,
            UserService.SystemUserProtectedException.class,
            UserService.LastAdminException.class
    })
    public ProblemDetail handleUserConflict(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/user-conflict"));
        problem.setTitle("User conflict");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({
            AttendanceService.MemberInactiveException.class,
            AttendanceService.AlreadyCheckedInException.class,
            AttendanceService.NotCheckedInException.class
    })
    public ProblemDetail handleAttendanceConflict(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://gms.example.com/errors/attendance-conflict"));
        problem.setTitle("Attendance conflict");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(AccessCodeService.AccessCodeNotFoundException.class)
    public ProblemDetail handleAccessCodeNotFound(AccessCodeService.AccessCodeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://gms.example.com/errors/access-code-not-found"));
        problem.setTitle("Access code not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }
}