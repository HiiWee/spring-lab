package com.example.springsecurity.config;

import com.example.springsecurity.config.exception.RestAccessDeniedHandler;
import com.example.springsecurity.config.exception.RestAuthenticationEntryPoint;
import com.example.springsecurity.support.token.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper mapper;

    public SecurityConfig(final JwtTokenProvider jwtTokenProvider, final ObjectMapper mapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.mapper = mapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .antMatchers("/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 설정 Disable
        http.csrf().disable()
                .cors().and()
                .exceptionHandling()
                // 인증 실패시 예외 처리(사용자가 인증되지 않음)
                .authenticationEntryPoint(new RestAuthenticationEntryPoint(mapper))
                // 인가 실패시 예외 처리 (허락되지 않은 접근)
                .accessDeniedHandler(new RestAccessDeniedHandler(mapper))
                .and()
                // JWT를 사용하므로 세션 사용하지 않음
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 로그인, 회원가입 API는 토큰이 없는 상태에서 요청이 들어오기 때문에 permitAll 설정
                .and()
                .authorizeRequests()
                .antMatchers("/signin").permitAll()
                .antMatchers("/members/signup").permitAll()
                .anyRequest().authenticated()
                .and()
                .apply(new JwtSecurityConfig(jwtTokenProvider, mapper))
                .and()
                .formLogin().disable();
        return http.build();
    }
}
