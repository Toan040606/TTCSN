package vn.edu.haui.bachmai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;
import vn.edu.haui.bachmai.dto.ApiDtos.*;
import vn.edu.haui.bachmai.repository.UserAccountRepository;
import vn.edu.haui.bachmai.security.JwtService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final JwtService jwt;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = users.findByUsername(request.username()).orElseThrow();
        return new LoginResponse(jwt.generate(user.getUsername(), user.getRole().name()), user.getId(),
            user.getUsername(), user.getFullName(), user.getRole());
    }
}
