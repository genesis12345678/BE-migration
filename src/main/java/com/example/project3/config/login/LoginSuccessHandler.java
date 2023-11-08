package com.example.project3.config.login;

import com.example.project3.Entity.Member;
import com.example.project3.repository.MemberRepository;
import com.example.project3.service.RefreshTokenService;
import com.example.project3.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        String email = extractUsername(authentication); // 인증 정보에서 Username(email) 추출
        String accessToken = tokenService.createAccessToken(email); // tokenService의 createAccessToken을 사용하여 AccessToken 발급
        String refreshToken = tokenService.createRefreshToken(email); // tokenService의 createRefreshToken을 사용하여 RefreshToken 발급

        tokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken); // 응답 헤더에 AccessToken, RefreshToken 실어서 응답

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("Unexpected Member"));

        refreshTokenService.saveRefreshToken(member.getId(), refreshToken);


        log.info("로그인에 성공하였습니다. 이메일 : {}", email);
        log.info("로그인에 성공하였습니다. AccessToken : {}", accessToken);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("userEmail : {}", userDetails.getUsername());
        return userDetails.getUsername();
    }
}
