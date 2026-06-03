package com.identityservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter đọc JWT từ header Authorization và đặt thông tin xác thực vào SecurityContext.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(jwtProperties.getHeaderName());
        String tokenPrefix = jwtProperties.getTokenPrefix() == null ? "Bearer" : jwtProperties.getTokenPrefix().trim();
        String bearerPrefix = tokenPrefix + " ";

        if (authorizationHeader == null || !authorizationHeader.startsWith(bearerPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(bearerPrefix.length()).trim();
        if (token.isBlank() || SecurityContextHolder.getContext().getAuthentication() != null || !jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractSubject(token);
        CustomUserDetail userDetail = (CustomUserDetail) customUserDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetail,
                null,
                userDetail.getAuthorities()
        );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
