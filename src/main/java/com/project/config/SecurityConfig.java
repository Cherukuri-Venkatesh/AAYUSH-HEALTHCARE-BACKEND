package com.project.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.project.jwt.JwtFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("https://aayush-healthcare-frontend.vercel.app");
        configuration.addAllowedOriginPattern("https://*.vercel.app");
        configuration.addAllowedOriginPattern("http://localhost:5173");
        configuration.addAllowedOriginPattern("http://localhost:3000");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            // ✅ Stateless session (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden")
                )
            )

            .authorizeHttpRequests(auth -> auth

                // ✅ Preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ Public APIs
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/patients/register").permitAll()

                // ✅ Public read-only hospital APIs
                .requestMatchers(HttpMethod.GET, "/api/hospitals/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/patient/doctors").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/patient/doctors/top").permitAll()

                // 🔐 Admin access to patient management endpoints
                .requestMatchers(HttpMethod.GET, "/api/patients/all").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/patients/*").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/patients/delete/**").hasAuthority("ADMIN")

                // 🔐 Admin appointments overview
                .requestMatchers(HttpMethod.GET, "/api/appointments/all").hasAuthority("ADMIN")

                // 🔐 ADMIN ONLY
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // 🔐 DOCTOR ONLY
                .requestMatchers("/api/doctor/**").hasAnyAuthority("DOCTOR", "ADMIN")

                // 🔐 PATIENT ONLY
                .requestMatchers("/api/patient/**").hasAnyAuthority("PATIENT", "ADMIN")

                // 🔐 LEGACY ROUTES (kept for backward compatibility)
                .requestMatchers(HttpMethod.POST, "/api/appointments/book").hasAnyAuthority("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/appointments/patient/**").hasAnyAuthority("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/appointments/cancel").hasAnyAuthority("PATIENT", "ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/appointments/doctor/**").hasAnyAuthority("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/appointments/doctor/cancel").hasAnyAuthority("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/appointments/status/**").hasAnyAuthority("DOCTOR", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/schedule/add").hasAnyAuthority("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/schedule/update/**").hasAnyAuthority("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/schedule/delete/**").hasAnyAuthority("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/schedule/**").hasAnyAuthority("PATIENT", "DOCTOR", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/prescriptions/upload").hasAnyAuthority("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/prescriptions/**").hasAnyAuthority("PATIENT", "DOCTOR", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/labreports/upload").hasAnyAuthority("DOCTOR", "ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/patient-history/**").hasAnyAuthority("PATIENT", "DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/files/download").hasAnyAuthority("PATIENT", "DOCTOR", "ADMIN")

                // 🔐 Everything else
                .anyRequest().authenticated()
            )

            // ✅ JWT Filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
