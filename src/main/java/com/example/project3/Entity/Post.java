package com.example.project3.Entity;

import com.example.project3.dto.request.PostRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long postId;
    private String postContent;
    //위치
    @Column(nullable = false)
    private String postLocation;

    //위도
    @Column(nullable = false)
    private double latitude;

    //경도
    @Column(nullable = false)
    private double longitude;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<MediaFile> medias = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    private LocalDateTime createdAt;


    @JoinColumn(name = "memberId")
    @ManyToOne
    private Member member;


    private int countLiked; // 좋아요 수

    @PrePersist // 디비에 INSERT 되기 직전에 실행
    public void createAt() {
        this.createdAt = LocalDateTime.now();
    }

    public static Post fromDto(PostRequestDto requestDto, Member member) {
        return Post.builder()
                .postLocation(requestDto.getLocation())
                .postContent(requestDto.getContent())
                //.hashtags(createHashtags(requestDto.getHashtags(), post)) // 추가: 해시태그 설정
                .member(member)
                .build();

    }

    // 추가: 해시태그 생성 및 설정
    private static List<PostHashtag> createHashtags(List<String> hashtagNames, Post post) {
        return hashtagNames.stream()
                .map(hashtagName -> new PostHashtag(post, new Hashtag(hashtagName)))
                .collect(Collectors.toList());
    }
}
