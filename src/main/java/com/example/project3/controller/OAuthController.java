//package com.example.project3.controller;
//
//import com.example.project3.dto.request.SocialUserSignupRequest;
//import com.example.project3.service.MemberService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/oauth")
//public class OAuthController {
//
//    private final MemberService memberService;
//
//    @PostMapping("/signup")
//    public void signupSocialUser(@RequestBody SocialUserSignupRequest request
//    , HttpServletRequest httpRequest, HttpServletResponse response) {
//
//        String token = httpRequest.getHeader("Authorization");
//        memberService.signupSocialUser(token, request, response);
//
//    }
//}
