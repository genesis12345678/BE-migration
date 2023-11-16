package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.service.MemberService;
import com.example.project3.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final S3Uploader s3Uploader;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestPart("request") SignupRequest request, @RequestPart("file")MultipartFile file) throws IOException {
        log.info("회원가입 요청이 들어왔습니다.");

        log.info("userName = {}",request.getUserName());
        log.info("email = {}",request.getEmail());
        log.info("address = {}", request.getAddress());
        log.info("imageFile = {}", file.getOriginalFilename());
        log.info("nickName = {}", request.getNickName());
        log.info("message = {}", request.getMessage());

        String url = s3Uploader.uploadProfileImage(file);

        return memberService.signup(request, url);
    }


    @PostMapping("/test")
    public String test() {

        return "인증 객체 접근 허용";
    }

}