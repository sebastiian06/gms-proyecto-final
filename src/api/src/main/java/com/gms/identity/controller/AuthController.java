package com.gms.identity.controller;

import com.gms.identity.dto.LoginRequest;
import com.gms.identity.dto.LoginResponse;
import com.gms.identity.service.IdentityFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IdentityFacade identityFacade;

    public AuthController(IdentityFacade identityFacade) {
        this.identityFacade = identityFacade;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = identityFacade.login(
                request.username(),
                request.password()
        );
        return ResponseEntity.ok(response);
    }
}