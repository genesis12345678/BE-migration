package com.example.project3.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoId;
    private String postVideoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    public PostImage(String postVideoUrl, Post post) {
//        this();
//        this.post = post;
//        this.postVideoUrl = postVideoUrl;
//    }

}
