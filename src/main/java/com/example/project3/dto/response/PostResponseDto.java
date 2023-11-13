package com.example.project3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PostResponseDto {
    private Long postId;
    private Long userId;
    private String userImg;
    private String userName;
    private LocalDateTime date;
    private String location;
    private Float temperature;
    private List<String> mediaUrls;
    private String content;
    private Boolean liked;
    private int likedCount;
    private List<String> hashtagNames;
}
