package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청이 들어왔습니다.");

        log.info("userName = {}",request.getUserName());
        log.info("email = {}",request.getEmail());
        log.info("address = {}", request.getAddress());
        log.info("imageURL = {}", request.getImageURL());
        log.info("nickName = {}", request.getNickName());
        log.info("message = {}", request.getMessage());

        return memberService.signup(request);
    }


    @PostMapping("/test")
    public String test() {

        return "인증 객체 접근 허용";
    }

}