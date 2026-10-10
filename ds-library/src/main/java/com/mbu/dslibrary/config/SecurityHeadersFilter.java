package com.mbu.dslibrary.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Adds browser-side protection headers to every response.
 * Scripts, styles, fonts, frames and workers may only come from this site or the few CDNs the pages use
 * (Bootstrap, PDF.js, StPageFlip, highlight.js, Monaco, Pyodide, YouTube no-cookie player).
 * 'unsafe-eval' is needed by the Monaco editor, Pyodide and the in-browser JavaScript runner;
 * 'unsafe-inline' for styles is needed by Monaco and a few inline style attributes. There are no inline scripts.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self' 'unsafe-eval' blob: https://cdn.jsdelivr.net https://cdnjs.cloudflare.com",
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com",
            "font-src 'self' data: https://cdn.jsdelivr.net https://fonts.gstatic.com",
            "img-src 'self' data: blob:",
            "connect-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com",
            "worker-src 'self' blob: https://cdn.jsdelivr.net https://cdnjs.cloudflare.com",
            "frame-src https://www.youtube-nocookie.com",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
        chain.doFilter(request, response);
    }
}
