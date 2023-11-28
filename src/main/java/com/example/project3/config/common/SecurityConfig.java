package com.example.project3.config.common;

import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.config.login.*;
import com.example.project3.repository.MemberRepository;
import com.example.project3.service.CustomOAuth2UserService;
import com.example.project3.service.MemberDetailService;
import com.example.project3.service.MemberService;
import com.example.project3.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig{

    private final MemberDetailService memberDetailService;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthenticationEntryPoint authenticationEntryPoint;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                    http
                    .csrf().disable()
                    .cors().configurationSource(corsConfigurationSource())

                    .and()



                    .formLogin().disable()
                    .httpBasic().disable()

                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                    .and()

                    .authorizeRequests()
                    .antMatchers("/","/css/**","/images/**","/js/**","/favicon.ico").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/post").hasRole("USER")
                    .antMatchers( "/**/signup", "/login","/api/posts/**", "/api/post/*/likers", "/api/user/**").permitAll()
                    .antMatchers("/api/v2/**", "/swagger-ui.html","/swagger/**",
                                "/swagger-resources/**","/webjars/**","/v2/api-docs").permitAll()
                    .anyRequest().authenticated()

                    .and()

                    .exceptionHandling()
                            .accessDeniedHandler(((request, response, accessDeniedException) -> {
                                log.error("접근 권한 부족입니다.");
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setCharacterEncoding("UTF-8");
                                response.getWriter().write("접근 권한이 없습니다, 소셜유저 회원가입을 마무리 해야 합니다.");
                            }))
                    .authenticationEntryPoint(authenticationEntryPoint)

                    .and()

                    .oauth2Login()
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
                    .userInfoEndpoint().userService(customOAuth2UserService);

                http.addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class);
                http.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class);

                   return  http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
        return new JwtAuthenticationProcessingFilter(tokenService, tokenProvider, memberService);
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