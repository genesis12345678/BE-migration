package com.example.project3.Entity;

import com.example.project3.Entity.member.Member;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class PostLiked {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postLikedId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    // true = 좋아요 false = 좋아요 취소
    @Column
    private boolean liked; //int or enum 고려해보기

//    public PostLiked(Post post, User user) {
//        this.post = post;
//        this.user = user;
//        this.status = true;
//    }


}
