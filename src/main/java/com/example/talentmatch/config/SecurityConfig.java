package com.example.talentmatch.config;

import com.example.talentmatch.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtFilter jwtFilter,
            CustomUserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource
    ) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(

                                "/auth/register",
                                "/auth/**",
                                "/register",
                                "/login",

                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"

                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}