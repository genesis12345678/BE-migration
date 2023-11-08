package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        log.info("userName = {}",request.getUserName());
        log.info("email = {}",request.getEmail());
        log.info("address = {}", request.getAddress());
        log.info("imageURL = {}", request.getImageURL());
        log.info("nickName = {}", request.getNickName());
        log.info("phoneNumber = {}", request.getPhoneNumber());
        log.info("gender = {}", request.getGender());

        return memberService.signup(request);
    }


    @PostMapping("/test")
    public String test() {
        return "test";
    }
}