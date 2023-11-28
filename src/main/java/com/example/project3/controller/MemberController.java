package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.dto.request.UpdateUserInfoRequest;
import com.example.project3.dto.response.MemberInfoResponse;
import com.example.project3.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
    public ResponseEntity<MemberInfoResponse> getMemberInfo(@AuthenticationPrincipal UserDetails userDetails
    ,@PageableDefault Pageable pageable) {
        log.info("회원정보 조회 요청이 들어왔습니다.");
        MemberInfoResponse userInfo = memberService.getMemberInfo(userDetails.getUsername(), pageable);
        return ResponseEntity.ok().body(userInfo);
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        log.info("회원탈퇴 요청이 들어왔습니다.");
        log.info("email : {}", userDetails.getUsername());
        String accessToken = request.getHeader("Authorization");

        memberService.deleteAccount(userDetails.getUsername(), accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails , HttpServletRequest request) {
        log.info("로그아웃 요청이 들어왔습니다.");
        String accessToken = request.getHeader("Authorization");

        memberService.logout(userDetails, accessToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{nickName}")
    public ResponseEntity<Void> isDuplicatedNickname(@PathVariable String nickName) {
        log.info("닉네임 중복확인 요청이 들어왔습니다.");

        if (memberService.checkDuplicateNickname(nickName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        else return ResponseEntity.ok().build();
    }

    @PatchMapping("/user")
    public ResponseEntity<Void> updateUserInfo(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestPart(value = "request",required = false) UpdateUserInfoRequest request,
                                               @RequestPart(value = "file",required = false) MultipartFile file) {
        log.info("회원정보 수정 요청이 들어왔습니다.");
        String email = userDetails.getUsername();
        memberService.updateUserInfo(email, request, file);

        return ResponseEntity.ok().build();
    }
}