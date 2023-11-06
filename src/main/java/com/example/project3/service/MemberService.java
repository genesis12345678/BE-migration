package com.example.project3.service;

import com.example.project3.Entity.Member;
import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.dto.request.LoginRequest;
import com.example.project3.dto.request.SignupRequest;
import com.example.project3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private static final String DEFAULT_IMAGE_URL = "https://meatwiki.nii.ac.jp/confluence/images/icons/profilepics/anonymous.png";

    public ResponseEntity<String> signup(SignupRequest request) {

        if (isExist(request.getEmail())) {
            log.error("중복 이메일, 이미 가입된 정보임.");
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        }

        String imageURL = request.getImageURL() != null ? request.getImageURL() : DEFAULT_IMAGE_URL;
        memberRepository.save(Member.builder()
                                    .name(request.getUserName())
                                    .email(request.getEmail())
                                    .password(passwordEncoder.encode(request.getPassword()))
                                    .address(request.getAddress())
                                    .imageURL(imageURL)
                                    .nickName(request.getNickName())
                                    .gender(request.getGender())
                                    .phoneNumber(request.getPhoneNumber())
                                    .build());

        return new ResponseEntity<>("Signup Successful", HttpStatus.OK);

    }

    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new IllegalArgumentException("가입된 정보가 없는 이메일"));

        if (isExist(request.getEmail()) && passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return tokenProvider.generateToken(member, Duration.ofHours(1));
        } else if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 확인해주세요.");
        }

        return null;
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected member"));
    }

    private boolean isExist(String email){
        return memberRepository.existsByEmail(email);
    }
}