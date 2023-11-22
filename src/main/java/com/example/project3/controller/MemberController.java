package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.dto.response.MemberInfoResponse;
import com.example.project3.service.MemberService;
import com.example.project3.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestPart("request") SignupRequest request, @RequestPart(value = "file",required = false)MultipartFile file){
        log.info("회원가입 요청이 들어왔습니다.");

        log.info("userName = {}",request.getUserName());
        log.info("email = {}",request.getEmail());
        log.info("address = {}", request.getAddress());
        log.info("imageFile = {}", file.getOriginalFilename());
        log.info("nickName = {}", request.getNickName());
        log.info("message = {}", request.getMessage());

        return memberService.signup(request, file);
    }


    @GetMapping("/user")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(@AuthenticationPrincipal UserDetails userDetails) {
        MemberInfoResponse userInfo = memberService.getMemberInfo(userDetails.getUsername());
        return ResponseEntity.ok().body(userInfo);
    }
}