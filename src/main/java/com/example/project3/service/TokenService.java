package com.example.project3.service;

import com.example.project3.Entity.Member;
import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;

    private final static String ACCESS_TOKEN_HEADER = "Authorization_Access_Token";
    private final static String REFRESH_TOKEN_HEADER = "Authorization_Refresh_Token";

    // AccessToken이 만료되었을 때 새로운 AccessToken을 발급
    public String createNewAccessToken(String refreshToken) {
        if (!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Unexpected token");
        }

        Long memberId = refreshTokenService.findByRefreshToken(refreshToken).getMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new IllegalArgumentException("Cannot Found Member"));

        return tokenProvider.createAccessToken(member);
    }

    // AccessToken 생성
    public String createAccessToken(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("Cannot Found Member"));

        return tokenProvider.createAccessToken(member);
    }

    // RefreshToken 생성
    public String createRefreshToken(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("Cannot Found Member"));

        return tokenProvider.createRefreshToken(member);
    }

    // AccessToken과 RefreshToken을 헤더에 실어서 응답
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_CREATED);

        response.setHeader(ACCESS_TOKEN_HEADER, accessToken);
        response.setHeader(REFRESH_TOKEN_HEADER, refreshToken);

        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    // AccessToken을 헤더에 실어서 응답
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_CREATED);

        response.setHeader(ACCESS_TOKEN_HEADER, accessToken);

        log.info("재발급된 Access Token : {}", accessToken);
    }
}