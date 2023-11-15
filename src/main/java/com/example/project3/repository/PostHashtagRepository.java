package com.example.project3.repository;

import com.example.project3.Entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
    List<PostHashtag> findByPost_PostIdAndHashtag_HashtagName(Long postId, String hashtagName);
}

