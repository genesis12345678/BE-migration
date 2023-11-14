package com.example.project3.config.login;

import com.example.project3.dto.oauth2.CustomOAuth2User;
import com.example.project3.Entity.member.Role;
import com.example.project3.repository.MemberRepository;
import com.example.project3.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    private final static String ACCESS_TOKEN_HEADER = "Authorization_Access_Token";
    private final static String REFRESH_TOKEN_HEADER = "Authorization_Refresh_Token";
    private final static String BEARER = "Bearer ";
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공");
        log.info("OAuth2LoginSuccessHandler가 실행됩니다");

        try{
            CustomOAuth2User oAuth2User =(CustomOAuth2User) authentication.getPrincipal();
            log.info("oAuth2USer : {}", oAuth2User);

            // Role이 GUEST일 경우 처음 요청한 회원이므로 추가정보를 위해 회원가입 페이지 리다이렉트
            if (oAuth2User.getRole() == Role.GUEST) {
                String accessToken = tokenService.createAccessToken(oAuth2User.getEmail());
                // TODO : 프론트의 회원가입 추가 정보 입력 폼으로 리다이렉트
//                response.sendRedirect();

                tokenService.sendAccessAndRefreshToken(response, accessToken, null);

                // TODO : 회원가입 추가 폼 입력 시 Role을 "USER"로 업데이트하는 컨트롤러와 서비스 로직 작성 필요
//                Member member = memberRepository.findByEmail(oAuth2User.getEmail())
//                        .orElseThrow(() -> new IllegalArgumentException("Cannot Found Member"));
//                member.authorizeMember();
            } else loginSuccess(response,oAuth2User);
        }catch(Exception e){
            throw e;
        }



    }

    // TODO : 소셜 로그인 시에 무조건 토큰 생성 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리해보기
    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) {
        String accessToken = tokenService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = tokenService.createRefreshToken(oAuth2User.getEmail());

        tokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        tokenService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
    }
}
