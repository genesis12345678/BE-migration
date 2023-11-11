package com.example.project3.config.common;

import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.config.login.CustomJsonUsernamePasswordAuthenticationFilter;
import com.example.project3.config.login.LoginFailureHandler;
import com.example.project3.config.login.LoginSuccessHandler;
import com.example.project3.repository.MemberRepository;
import com.example.project3.service.MemberDetailService;
import com.example.project3.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig{

    private final MemberDetailService memberDetailService;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;


    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .antMatchers("/api/v2/**", "/swagger-ui.html","/swagger/**",
                        "/swagger-resources/**","/webjars/**","/v2/api-docs");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                    .csrf().disable()
                    .cors().and()

                    .formLogin().disable()
                    .httpBasic().disable()

                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                    .and()

                    .authorizeRequests()

                    .antMatchers( "/signup", "/login").permitAll()
                    .antMatchers("/test").authenticated()
                    .antMatchers("/user").hasRole("USER")

                    .and()

                    .addFilterBefore(customJsonUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(jwtAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)

                    .build();
    }


    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(memberDetailService);
        return new ProviderManager(provider);
    }


    @Bean
    public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter(){
        CustomJsonUsernamePasswordAuthenticationFilter filter =  new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(loginSuccessHandler());
        filter.setAuthenticationFailureHandler(loginFailureHandler());
        return filter;
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(tokenService, memberRepository, tokenProvider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(tokenService, memberRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }
}