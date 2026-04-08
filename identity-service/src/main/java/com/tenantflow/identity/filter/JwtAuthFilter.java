package com.tenantflow.identity.filter;

import com.tenantflow.identity.context.TenantContext;
import com.tenantflow.identity.entity.User;
import com.tenantflow.identity.service.JwtService;
import com.tenantflow.identity.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7);

            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Blacklisted token used");
                filterChain.doFilter(request, response);
                return;
            }

            final String email = jwtService.extractEmail(token);
            final String tenantId = jwtService.extractTenantId(token);
            final String role = jwtService.extractRole(token);

            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                User userDetails = (User) userDetailsService
                        .loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {

                    TenantContext.setTenantId(tenantId);

                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            List.of(new SimpleGrantedAuthority(
                                    "ROLE_" + role))
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug("Authenticated user: {} for tenant: {}",
                            email, tenantId);
                }
            }

        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
        } finally {
            filterChain.doFilter(request, response);
            TenantContext.clear();
        }
    }
}
