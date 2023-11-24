package com.example.project3.repository;

import com.example.project3.Entity.member.Member;
import com.example.project3.Entity.Post;
import com.example.project3.Entity.PostLiked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostLikedRepository extends JpaRepository<PostLiked, Long> {

    PostLiked findByPostAndMember(Post post, Member member);

    boolean existsByPostAndMember(Post post, Member member);

    List<PostLiked> findByPost_PostIdAndLiked(Long postId, boolean liked);

    List<PostLiked> findByPost_PostId(Long postId);
}
