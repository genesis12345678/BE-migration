package com.example.project3.controller;

import com.example.project3.dto.request.SignupRequest;
import com.example.project3.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<?> test1(@Valid @RequestBody SignupRequest request, BindingResult bindingResult) {
        log.info("userName = {}",request.getUserName());
        log.info("email = {}",request.getEmail());
        log.info("address = {}", request.getAddress());
        log.info("imageURL = {}", request.getImageURL());
        log.info("nickName = {}", request.getNickName());
        log.info("phoneNumber = {}", request.getPhoneNumber());
        log.info("gender = {}", request.getGender());

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        return memberService.signup(request);
    }
}