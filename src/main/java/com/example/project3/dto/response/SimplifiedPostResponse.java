package com.example.project3.dto.response;

import com.example.project3.Entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Getter
@AllArgsConstructor
public class SimplifiedPostResponse {
    private Long postId;
    private LocalDateTime createdAt;
    private List<String> mediaUrls;

    public SimplifiedPostResponse(Post post) {
        this.postId = post.getPostId();
        this.createdAt = post.getCreatedAt();
        // MediaFile이 존재하는 경우에만 첫 번째 MediaFile의 fileUrl을 추가
        this.mediaUrls = post.getMedias().isEmpty()
                            ? Collections.emptyList()
                            : Collections.singletonList(post.getMedias().get(0).getFileUrl());
    }
}