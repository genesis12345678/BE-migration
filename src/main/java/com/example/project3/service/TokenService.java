package com.example.project3.service;

import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class TokenService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    private final static String ACCESS_TOKEN_HEADER = "Authorization_Access_Token";
    private final static String REFRESH_TOKEN_HEADER = "Authorization_Refresh_Token";
    private final static String BEARER = "Bearer ";

    // AccessToken이 만료되었을 때 새로운 AccessToken을 발급
    @Transactional
    public void createNewAccessToken(String refreshToken, HttpServletResponse response) {

        memberRepository.getEmailByRefreshToken(refreshToken).ifPresentOrElse(email -> {
            String newAccessToken = createAccessToken(email);
            String newRefreshToken = createRefreshToken();
            sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

            memberRepository.updateRefreshToken(refreshToken, newRefreshToken);

            log.info("새로운 AccessToken, RefreshToken 응답 및 DB 저장 성공");
        },
        () -> {
            throw new EntityNotFoundException("조회 실패");
        });
    }

    // AccessToken 생성
    public String createAccessToken(String email) {
        log.info("createAccessToken");
        Long id = getId(email);

        return tokenProvider.createAccessToken(email, id);
    }

    // RefreshToken 생성
    public String createRefreshToken() {
        log.info("createRefreshToken");

        return tokenProvider.createRefreshToken();
    }


    // AccessToken과 RefreshToken을 헤더에 실어서 응답
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_CREATED);

        response.setHeader(ACCESS_TOKEN_HEADER, BEARER + accessToken);
        response.setHeader(REFRESH_TOKEN_HEADER, BEARER + refreshToken);

        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {

        memberRepository.setRefreshToken(email, refreshToken);
    }

    private Long getId(String email) {
        return memberRepository.getIdMyEmail(email)
                .orElseThrow(EntityNotFoundException::new);
    }
}