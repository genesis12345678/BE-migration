package com.example.project3.controller;

import com.example.project3.dto.request.PostRequestDto;
import com.example.project3.dto.request.PostUpdateRequestDto;
import com.example.project3.dto.response.PostResponseDto;
import com.example.project3.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

//@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class PostController {
    //private final PostService postService;
    @Autowired
    private PostService postService;


    public static final int DEFAULT_PAGE_SIZE = 10;

    @PostMapping("/post")
    public ResponseEntity<String> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            PostRequestDto postRequestDto) {


        Long postId = postService.createPost(userDetails.getUsername(), postRequestDto);
        String message = "게시물이 성공적으로 등록되었습니다.";

        return ResponseEntity.status(HttpStatus.OK)
                .body(postId + message);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponseDto>> firstMainList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "" + Long.MAX_VALUE) Long lastPostId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = DEFAULT_PAGE_SIZE)
            Pageable pageable) {

        if (userDetails != null) {
            // 로그인한 경우
            Page<PostResponseDto> allPostList = postService.getAllPostList(lastPostId, pageable, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(allPostList);
        } else {
            // 로그인하지 않은 경우
            Page<PostResponseDto> allPostList = postService.getAllPostList(lastPostId, pageable, null);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(allPostList);
        }

    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostResponseDto postResponseDto = postService.getPostById(postId, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.OK)
                .body(postResponseDto);
    }

    @PutMapping("/post/update/{postId}")
    public ResponseEntity<PostResponseDto> updatePost1(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            PostUpdateRequestDto postUpdateRequestDto) {

        if (postUpdateRequestDto.getNewPostImages() != null && postUpdateRequestDto.getNewPostImages().size() + postUpdateRequestDto.getOriginalImages().size() > 3) {
            throw new IllegalArgumentException("사진은 3장만 등록가능합니다.");
        }
        if (postUpdateRequestDto.getNewPostImages() == null) {
            postUpdateRequestDto.setNewPostImages(Collections.emptyList());
        }



        PostResponseDto postResponseDto = postService.updatePost2(postId, userDetails.getUsername(), postUpdateRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(postResponseDto);

    }



    @PostMapping("/post/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isLiked = postService.toggleLike(postId, userDetails.getUsername());

        if (isLiked) {
            return ResponseEntity.status(HttpStatus.CREATED).body("좋아요가 추가되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("이미 좋아요를 누르셨습니다.");
        }

    }



}