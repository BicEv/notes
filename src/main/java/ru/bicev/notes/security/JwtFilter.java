package ru.bicev.notes.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.bicev.notes.service.JwtService;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // This one is for tests, because they don't works with getServletPath() with
        // mockMvc
        if (path.isEmpty()) {
            path = request.getRequestURI();
        }

        logger.debug("Path : {}", path);
        List<String> publicPaths = List.of(
                "/api/users/register", "/api/users/login",
                "/swagger-ui", "/swagger-ui.html",
                "/v3/api-docs", "/swagger-resources", "/webjars");

        if (publicPaths.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                String email = jwtService.extractUsername(jwt);
                logger.debug("JWT received: {}", jwt);
                if (jwtService.isTokenValid(jwt, email)) {

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                            List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("User authenticated: {}", email);

                } else {
                    logger.warn("Token is invalid: {}", jwt);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }
            } catch (IllegalArgumentException e) {
                logger.error("Invalid token format", e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token format");
                return;
            }
        } else {
            logger.warn("Invalid or missing authHeader: {}", authHeader);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Authorization header missing or invalid");
            return;
        }
        filterChain.doFilter(request, response);

    }

}
