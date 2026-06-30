package org.shub.postservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.parseClaims(token);
            String userId = claims.getSubject();

            // Equivalent of populating ClaimsPrincipal.User with the JWT's
            // claims so controllers can read ClaimTypes.NameIdentifier, etc.
            var authentication = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(userId, claims),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            //Setting Current Authenticated user in Security Context which holds whole authenticated users
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid/expired token: leave SecurityContext empty so the
            // request is treated as anonymous, same as ASP.NET would for a
            // failed JwtBearer challenge - downstream .authenticated() checks
            // will reject it with 401/403 as appropriate.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
