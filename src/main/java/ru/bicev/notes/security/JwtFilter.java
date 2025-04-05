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
import ru.bicev.notes.dto.UserDto;
import ru.bicev.notes.exception.UserNotFoundException;
import ru.bicev.notes.service.JwtService;
import ru.bicev.notes.service.UserService;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private static Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public JwtFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String email = jwtService.extractUsername(jwt);
            logger.debug("JWT received: {}", jwt);
            if (jwtService.isTokenValid(jwt, email)) {
                try {
                    UserDto userDto = userService.getUserByEmail(email);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                            List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("User authenticated: {}", email);
                } catch (UserNotFoundException e) {
                    logger.warn("User not found for email in token: {}", email);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }
            } else {
                logger.warn("Token is invalid: {} or userDto: {}", jwt, email);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
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
