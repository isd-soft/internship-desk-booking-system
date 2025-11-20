package com.project.internship_desk_booking_system.config;

import com.project.internship_desk_booking_system.config.ldap.LdapHealthChecker;
import com.project.internship_desk_booking_system.config.ldap.LdapProperties;
import com.project.internship_desk_booking_system.config.ldap.SafeLdapAuthenticationProvider;
import com.project.internship_desk_booking_system.jwt.JwtFilter;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    private final JwtFilter jwtFilter;
    private final CustomAccessDeniedHandler deniedHandler;
    private final CustomAuthEntryPoint authEntryPoint;

    private final LdapProperties ldapProps;

    @Bean
    @ConditionalOnProperty(name = "app.ldap.enabled", havingValue = "true")
    public LdapContextSource ldapContextSource() {
        LdapContextSource ctx = new LdapContextSource();
        ctx.setUrl(ldapProps.getLdapUrl());
        ctx.setBase(ldapProps.getLdapBaseDn());
        ctx.setUserDn(ldapProps.getLdapUsername());
        ctx.setPassword(ldapProps.getLdapPassword());
        ctx.afterPropertiesSet();
        return ctx;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoProvider, @Autowired(required = false) LdapContextSource ldapContext, SafeLdapAuthenticationProvider ldapProvider) {
        List<AuthenticationProvider> providers = new ArrayList<>();

        if (ldapProps.isLdapEnabled() && ldapContext != null) {
            providers.add(ldapProvider);
        }

        providers.add(daoProvider);
        return new ProviderManager(providers);
    }

    @Bean
    public SafeLdapAuthenticationProvider safeLdapProvider(
            @Autowired(required = false) LdapContextSource ldapContext,
            LdapProperties ldapProperties,
            LdapHealthChecker ldapHealthChecker
    ) {
        return new SafeLdapAuthenticationProvider(ldapContext, ldapProperties, ldapHealthChecker);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(deniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder encoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return new CustomUserDetailsService(repo);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
