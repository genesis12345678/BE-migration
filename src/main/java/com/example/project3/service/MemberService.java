package com.example.project3.service;

import com.example.project3.Entity.member.Member;
import com.example.project3.Entity.member.Role;
import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.dto.request.SignupRequest;
import com.example.project3.dto.request.SocialUserSignupRequest;
import com.example.project3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;

    public static final String DEFAULT_IMAGE_URL = "https://meatwiki.nii.ac.jp/confluence/images/icons/profilepics/anonymous.png";

    public ResponseEntity<String> signup(SignupRequest request,String fileUrl) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (isExist(request.getEmail())) {
            log.error("중복 이메일, 이미 가입된 정보임.");
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        }

        String imageURL = (fileUrl != null) ? fileUrl : DEFAULT_IMAGE_URL;

        memberRepository.save(Member.builder()
                                    .name(request.getUserName())
                                    .email(request.getEmail())
                                    .password(passwordEncoder.encode(request.getPassword()))
                                    .address(request.getAddress())
                                    .imageURL(imageURL)
                                    .nickName(request.getNickName())
                                    .message(request.getMessage())
                                    .role(Role.USER)
                                    .build());
        log.info("회원정보가 저장되었습니다.");

        return new ResponseEntity<>("Signup Successful", HttpStatus.OK);

    }

    private boolean isExist(String email) {
        return memberRepository.existsByEmail(email);
    }

    public void signupSocialUser(String token, SocialUserSignupRequest request, HttpServletResponse response) {
        log.info("소셜 유저 회원가입 실행");

        String email = tokenProvider.getMemberEmail(token);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cannot Found Member"));

        member.signupSocialUser(request.getMessage(), request.getAddress(), request.getNickName());

        String accessToken = tokenService.createAccessToken(email);
        String refreshToken = tokenService.createRefreshToken();

        member.updateRefreshToken(refreshToken);

        memberRepository.save(member);
        log.info("추가로 입력받은 정보로 GUEST -> USER로 변환하고 회원가입을 마무리합니다.");

        tokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        }

    }
