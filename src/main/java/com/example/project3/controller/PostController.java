package com.example.project3.controller;

import com.example.project3.dto.request.PostRequestDto;
import com.example.project3.dto.response.PostResponseDto;
import com.example.project3.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class PostController {
    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            PostRequestDto postRequestDto) {

        PostResponseDto postResponseDto = postService.createPost(userDetails.getUsername(), postRequestDto);

        System.out.println(postResponseDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(postResponseDto);
    }
}