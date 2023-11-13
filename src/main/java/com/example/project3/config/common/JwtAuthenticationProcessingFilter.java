package com.example.project3.config.common;

import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 출처 : https://ksh-coding.tistory.com/59#2.%20JWT%20%EC%9D%B8%EC%A6%9D%20%ED%95%84%ED%84%B0%20-%20JwtAuthenticationProcessingFilter-1
 * Jwt 인증 필터
 * "/login" 이외의 URI 요청이 왔을 때 처리하는 필터
 *
 * 기본적으로 사용자는 요청 헤더에 AccessToken만 담아서 요청
 * AccessToken 만료 시에만 RefreshToken을 요청 헤더에 AccessToken과 함께 요청
 *
 * 1. RefreshToken이 없고, AccessToken이 유효한 경우 -> 인증 성공 처리, RefreshToken을 재발급하지는 않는다.
 * 2. RefreshToken이 없고, AccessToken이 없거나 유효하지 않은 경우 -> 인증 실패 처리, 403 ERROR
 * 3. RefreshToken이 있는 경우 -> DB의 RefreshToken과 비교하여 일치하면 AccessToken 재발급, RefreshToken 재발급(RTR 방식)
 *                              인증 성공 처리는 하지 않고 실패 처리
 *
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login"; // "/login"으로 들어오는 요청은 Filter 작동 X

    private final TokenService tokenService;
    private final TokenProvider tokenProvider;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String HEADER_REFRESH_TOKEN_AUTHORIZATION = "Authorization_Refresh_Token";
    private final static String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("요청 URI : {}, 요청 메소드 : {}", request.getRequestURI() , request.getMethod());
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response); // "/login" 요청이 들어오면, 다음 필터 호출
            return; // return으로 이후 현재 필터 진행 막기 (안 해주면 아래로 내려가서 계속 필터 진행시킴)
        }

        // 사용자 요청 헤더에서 RefreshToken 추출
        // -> RefreshToken이 없거나 유효하지 않다면(DB에 저장된 RefreshToken과 다르다면) null을 반환
        // 사용자의 요청 헤더에 RefreshToken이 있는 경우는, AccessToken이 만료되어 요청한 경우밖에 없다.
        // 따라서, 위의 경우를 제외하면 추출한 refreshToken은 모두 null
        String authorizationHeader = request.getHeader(HEADER_REFRESH_TOKEN_AUTHORIZATION);
        String refreshToken = getRefreshToken(authorizationHeader);
        log.info("추출된 RefreshToken : {}", refreshToken);


        // 리프레시 토큰이 요청 헤더에 존재했다면, 사용자가 AccessToken이 만료되어서
        // RefreshToken까지 보낸 것이므로 리프레시 토큰이 DB의 리프레시 토큰과 일치하는지 판단 후,
        // 일치한다면 AccessToken을 재발급해준다.
        if (refreshToken != null) {
            log.info("checkRefreshTokenAndReIssueAccessToken 메소드 실행");
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return; // RefreshToken을 보낸 경우에는 AccessToken을 재발급 하고 인증 처리는 하지 않게 하기위해 바로 return으로 필터 진행 막기
        }

        // RefreshToken이 없거나 유효하지 않다면, AccessToken을 검사하고 인증을 처리하는 로직 수행
        // AccessToken이 없거나 유효하지 않다면, 인증 객체가 담기지 않은 상태로 다음 필터로 넘어가기 때문에 403 에러 발생
        // AccessToken이 유효하다면, 인증 객체가 담긴 상태로 다음 필터로 넘어가기 때문에 인증 성공
        if (refreshToken == null) {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }

    /**
     *  [리프레시 토큰으로 유저 정보 찾기 & 액세스 토큰/리프레시 토큰 재발급 메소드]
     *  파라미터로 들어온 헤더에서 추출한 리프레시 토큰으로 DB에서 유저를 찾고, 해당 유저가 있다면
     *  tokenService.createAccessToken()으로 AccessToken과 RefreshToken 생성,
     *  리프레시 토큰 재발급 & DB에 리프레시 토큰 업데이트 메소드 호출
     *  그 후 응답으로 새로운 AccessToken과 RefreshToken 각각 헤더로 응답
     */
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        log.info("새로운 AccessToken 생성 시작");
        tokenService.createNewAccessToken(refreshToken, response);
    }

    /**
     * [액세스 토큰 체크 & 인증 처리 메소드]
     * request에서 getAccessToken()으로 액세스 토큰 추출 후 유효한 토큰인지 검증
     * 유효한 토큰이면, 액세스 토큰으로 Authentication 객체 생성
     * 생성된 Authentication 객체를 SecurityContextHolder에 담기
     * 그 후 다음 인증 필터로 진행
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {

        log.info("checkAccessTokenAndAuthentication() 호출");

        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        // 요청 URI
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info("요청 URI : {}, 요청 메소드 : {}", requestURI, method);

        // 가져온 값에서 Bearer 제거
        String accessToken = getAccessToken(authorizationHeader);
        log.info("추출된 AccessToken : {}", accessToken);


        // 토큰이 유효한지 확인하고, 유효하면 인증 정보를 설정
        if (tokenProvider.validToken(accessToken)) {

            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
            log.info("저장된 Authentication 객체 : {}", authentication);
        }
        filterChain.doFilter(request, response);
    }
//
//    /**
//     * [인증 허가 메소드]
//     * 파라미터의 유저 : 우리가 만든 회원 객체 / 빌더의 유저 : UserDetails의 User 객체
//     *
//     * new UsernamePasswordAuthenticationToken()로 인증 객체인 Authentication 객체 생성
//     * UsernamePasswordAuthenticationToken의 파라미터
//     * 1. 위에서 만든 UserDetailsUser 객체 (유저 정보)
//     * 2. credential(보통 비밀번호로, 인증 시에는 보통 null로 제거)
//     * 3. Collection < ? extends GrantedAuthority>로,
//     * UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities이 있어서 getter로 호출한 후에,
//     * new NullAuthoritiesMapper()로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities()에 담기
//     *
//     * SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후,
//     * setAuthentication()을 이용하여 위에서 만든 Authentication 객체에 대한 인증 허가 처리
//     */
//    public void saveAuthentication(User myUser) {
//        String password = myUser.getPassword();
//        if (password == null) { // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
//            password = PasswordUtil.generateRandomPassword();
//        }
//
//        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
//                .username(myUser.getEmail())
//                .password(password)
//                .roles(myUser.getRole().name())
//                .build();
//
//        Authentication authentication =
//                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
//                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }

    // 헤더에서 AccessToken 추출
    // 헤더가 비어있거나 "Bearer "로 시작히지 않으면 null
    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    // 헤더에서 RefreshToken 추출
    // 헤더가 비어있거나 "Bearer "로 시작히지 않으면 null
    private String getRefreshToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }


}
