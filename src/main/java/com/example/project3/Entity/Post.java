package com.example.project3.Entity;

import com.example.project3.Entity.member.Member;
import com.example.project3.dto.request.PostUpdateRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<MediaFile> mediaFiles = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    private LocalDateTime createdAt;


    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member member;


    private int countLiked; // 좋아요 수

public void setPostHashtags(List<PostHashtag> postHashtags) {
    this.postHashtags = postHashtags;
}

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
    }

    public void setMediaFiles() {
        this.mediaFiles = new ArrayList<>();
    }

    public void addMediaFile(MediaFile mediaFile) {
        this.mediaFiles.add(mediaFile);
        mediaFile.setPost(this);
    }

    public void update(PostUpdateRequestDto requestDto) {
        this.postLocation = requestDto.getLocation();
        this.postTemperature = requestDto.getTemperature();
        this.postContent = requestDto.getContent();
    }

}