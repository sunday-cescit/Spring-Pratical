package com.gamesmicroservice.rest.filter;

import com.gamesmicroservice.rest.util.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
    System.out.println("🚀 Incoming request: " + request.getRequestURI());

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        System.out.println("🔒 No Bearer token found in Authorization header.");
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);

    try {
        Claims claims = jwtService.extractAllClaims(token);
        String username = claims.getSubject();
        Object rawRoles = claims.get("roles");

        List<SimpleGrantedAuthority> authorities = ((List<?>) rawRoles).stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        System.out.println("✅ Authenticated user: " + username);
        System.out.println("✅ Authorities: " + authorities);
    } catch (Exception e) {
        System.out.println("❌ JWT processing error: " + e.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
        return;
    }

    filterChain.doFilter(request, response);
}

}
