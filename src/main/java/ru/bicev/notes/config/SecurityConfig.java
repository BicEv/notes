package ru.bicev.notes.config;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ru.bicev.notes.security.JwtFilter;
import ru.bicev.repository.UserRepository;
import ru.bicev.notes.entity.User;
import ru.bicev.notes.exception.UserNotFoundException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserRepository userRepository, PasswordEncoder passwordEncoder)
            throws Exception {
        return authentication -> {
            var email = authentication.getPrincipal().toString();
            var password = authentication.getCredentials().toString();
            User user = userRepository.findByEmail(email).orElseThrow(() -> {
                return new UserNotFoundException("User not found");
            });

            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Invalid email: {} or password: {}", email, password);
                throw new BadCredentialsException("Invalid email or password");
            }
            logger.info("User authenticated: {}", email);
            return new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        };
    }

}
