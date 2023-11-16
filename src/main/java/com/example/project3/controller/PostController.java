package com.example.project3.controller;

import com.example.project3.dto.request.PostRequestDto;
import com.example.project3.dto.response.PostResponseDto;
import com.example.project3.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class PostController {
    private final PostService postService;
    public static final int DEFAULT_PAGE_SIZE = 10;

    @PostMapping("/post")
    //public ResponseEntity<PostResponseDto> createPost(
    public ResponseEntity<String> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            PostRequestDto postRequestDto) {

       // PostResponseDto postResponseDto = postService.createPost(userDetails.getUsername(), postRequestDto);
        String message = postService.createPost(userDetails.getUsername(), postRequestDto);

        //System.out.println(postResponseDto);

        return ResponseEntity.status(HttpStatus.OK)
                //.body(postResponseDto);
                .body(message);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponseDto>> firstMainList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "" + Long.MAX_VALUE) Long lastPostId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = DEFAULT_PAGE_SIZE)
            Pageable pageable) {

        Page<PostResponseDto> allPostList;

        if (userDetails == null) {
            // 사용자가 로그인되지 않은 경우
            allPostList = postService.getAllPostListForAnonymous(lastPostId, pageable);
        } else {
            // 사용자가 로그인한 경우
            allPostList = postService.getAllPostList(lastPostId, pageable, userDetails.getUsername());
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(allPostList);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostResponseDto postResponseDto = postService.getPostById(postId, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.OK)
                .body(postResponseDto);
    }





    @PostMapping("/post/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            // 사용자가 로그인되지 않았습니다. 에러 응답을 반환합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("게시물에 좋아요를 누르려면 로그인이 필요합니다.");
        }

        boolean isLiked = postService.toggleLike(postId, userDetails.getUsername());

        HttpStatus status = isLiked ? HttpStatus.CREATED : HttpStatus.NO_CONTENT;

        if (isLiked) {
            return ResponseEntity.status(HttpStatus.CREATED).body("좋아요가 추가되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("이미 좋아요를 누르셨습니다.");
        }
//        return ResponseEntity.status(status).build();
    }



}