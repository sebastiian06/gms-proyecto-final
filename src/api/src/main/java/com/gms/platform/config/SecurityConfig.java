
package com.gms.platform.config;

import com.gms.identity.service.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/attendance/entries").permitAll()
                        .requestMatchers(HttpMethod.POST, "/attendance/exits").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // Users: ADMIN only
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/**").hasRole("ADMIN")

                        // Members
                        .requestMatchers(HttpMethod.POST, "/members").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.GET, "/members/me").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.PATCH, "/members/*/trainer").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PATCH, "/members/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/members/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER")

                        // Plans
                        .requestMatchers(HttpMethod.POST, "/plans").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/plans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/plans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/plans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/plans/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER", "MEMBER")

                        // Memberships
                        .requestMatchers(HttpMethod.POST, "/memberships").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/memberships/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/memberships/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER", "MEMBER")

                        // Payments
                        .requestMatchers(HttpMethod.POST, "/memberships/*/payments").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.GET, "/memberships/*/payments").hasAnyRole("ADMIN", "RECEPTIONIST", "MEMBER")

                        // Trainers
                        .requestMatchers(HttpMethod.POST, "/trainers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/trainers/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER")

                        // Routines
                        .requestMatchers(HttpMethod.POST, "/routines/**").hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.PUT, "/routines/**").hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/routines/**").hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.GET, "/routines/**").hasAnyRole("ADMIN", "TRAINER", "MEMBER")

                        // Reports
                        .requestMatchers(HttpMethod.GET, "/reports/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/problem+json");
                            response.getWriter().write(
                                    "{\"type\":\"https://gms.example.com/errors/unauthorized\"," +
                                    "\"title\":\"Unauthorized\",\"status\":401," +
                                    "\"detail\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/problem+json");
                            response.getWriter().write(
                                    "{\"type\":\"https://gms.example.com/errors/forbidden\"," +
                                    "\"title\":\"Forbidden\",\"status\":403," +
                                    "\"detail\":\"Insufficient permissions\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
