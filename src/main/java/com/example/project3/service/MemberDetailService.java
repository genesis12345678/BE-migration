package com.example.project3.service;

import com.example.project3.Entity.member.Member;
import com.example.project3.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUserName 실행");
        log.info("로그인 시도");

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(email + "로 조회되는 Member가 없습니다."));
    }
}