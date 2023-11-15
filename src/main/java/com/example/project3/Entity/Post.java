package com.example.project3.Entity;

import com.example.project3.Entity.member.Member;
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
    private String postLocation;
    private Float postTemperature;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<MediaFile> medias = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    private LocalDateTime createdAt;


    @JoinColumn(name = "memberId")
    @ManyToOne
    private Member member;


    private int countLiked; // 좋아요 수

    // 좋아요 수 증가
    public void increaseCountLiked() {
        countLiked++;
    }

    // 좋아요 수 감소
    public void decreaseCountLiked() {
        countLiked = Math.max(countLiked - 1, 0);
    }

    @PrePersist // 디비에 INSERT 되기 직전에 실행
    public void createAt() {
        this.createdAt = LocalDateTime.now();
        this.medias = new ArrayList<>(); // 이 부분 추가
    }



    public void addMediaFile(MediaFile mediaFile) {
        this.medias.add(mediaFile);
        mediaFile.setPost(this);
    }

    // 추가: 해시태그 생성 및 설정
    private static List<PostHashtag> createHashtags(List<String> hashtagNames, Post post) {
        return hashtagNames.stream()
                .map(hashtagName -> new PostHashtag(post, new Hashtag(hashtagName)))
                .collect(Collectors.toList());
    }


}