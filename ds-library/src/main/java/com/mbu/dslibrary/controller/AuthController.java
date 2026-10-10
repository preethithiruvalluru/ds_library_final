package com.mbu.dslibrary.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Login, logout and "who am I" for the single admin account. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L;

    private record Attempts(int failures, long since) {}

    private final AuthenticationManager authManager;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public AuthController(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Attempts a = attempts.get(ip);
        long now = System.currentTimeMillis();
        if (a != null && now - a.since() < WINDOW_MS && a.failures() >= MAX_FAILURES) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("status", 429, "message", "Too many failed attempts. Try again in a few minutes."));
        }
        try {
            Authentication auth = authManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    body.getOrDefault("username", ""), body.getOrDefault("password", "")));
            attempts.remove(ip);
            HttpSession old = request.getSession(false);
            if (old != null) request.changeSessionId();   // new session id after login
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
            return ResponseEntity.ok(Map.of("authenticated", true, "username", auth.getName()));
        } catch (AuthenticationException e) {
            boolean fresh = a == null || now - a.since() >= WINDOW_MS;
            attempts.put(ip, new Attempts(fresh ? 1 : a.failures() + 1, fresh ? now : a.since()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Wrong username or password."));
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
        return Map.of("authenticated", false);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        boolean in = auth != null && auth.isAuthenticated();
        return in ? Map.of("authenticated", true, "username", auth.getName()) : Map.of("authenticated", false);
    }
}
