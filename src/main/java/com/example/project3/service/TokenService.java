package com.example.project3.service;

import com.example.project3.Entity.member.Member;
import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    private final static String ACCESS_TOKEN_HEADER = "Authorization_Access_Token";
    private final static String REFRESH_TOKEN_HEADER = "Authorization_Refresh_Token";
    private final static String BEARER = "Bearer ";

    // AccessToken이 만료되었을 때 새로운 AccessToken을 발급
    public void createNewAccessToken(String refreshToken, HttpServletResponse response){
        if (!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Unexpected token");
        }


        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()-> new IllegalArgumentException("Cannot Found Member"));

        String newAccessToken = createAccessToken(member.getEmail());
        String newRefreshToken = createRefreshToken(member.getEmail());

        sendAccessAndRefreshToken(response,newAccessToken, newRefreshToken);

        member.updateRefreshToken(newRefreshToken);
        memberRepository.save(member);

        log.info("새로운 AccessToken, RefreshToken 응답 및 DB 저장 성공");

    }

    // AccessToken 생성
    public String createAccessToken(String email) {
        log.info("createAccessToken");
        Member member = getMember(email);

        return tokenProvider.createAccessToken(member);
    }

    // RefreshToken 생성
    public String createRefreshToken(String email) {
        log.info("createRefreshToken");
        Member member = getMember(email);

        return tokenProvider.createRefreshToken(member);
    }

    private Member getMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("Cannot Found Member"));
    }

    // AccessToken과 RefreshToken을 헤더에 실어서 응답
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_CREATED);

        response.setHeader(ACCESS_TOKEN_HEADER, BEARER + accessToken);
        response.setHeader(REFRESH_TOKEN_HEADER, BEARER + refreshToken);

        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    public void updateRefreshToken(String email, String refreshToken) {
        memberRepository.findByEmail(email)
                .ifPresentOrElse(
                        member -> member.updateRefreshToken(refreshToken),
                        () -> new Exception("Cannot Found Member")
                );

    }
}