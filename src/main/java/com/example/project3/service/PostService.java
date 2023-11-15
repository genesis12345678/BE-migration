package com.example.project3.service;

import com.example.project3.Entity.*;
import com.example.project3.dto.request.PostRequestDto;
import com.example.project3.dto.response.PostResponseDto;
import com.example.project3.repository.HashtagRepository;
import com.example.project3.repository.MemberRepository;
import com.example.project3.repository.PostHashtagRepository;
import com.example.project3.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final HashtagRepository hashtagRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public PostResponseDto createPost(String username, PostRequestDto requestDto) {

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(()->new IllegalArgumentException("가입된 정보가 없는 이메일"));

        Post post = Post.builder()
                .postLocation(requestDto.getLocation())
                .postContent(requestDto.getContent())
                .member(member)
                .build();
        // DB에 저장
        Post savedPost = postRepository.save(post);

        System.out.println("savedPost 저장 후 " + savedPost);

        // MediaFiles 처리
        //List<String> mediaUrls = saveMediaFiles(requestDto.getMediaFiles());
        List<String> mediaUrls = saveMediaFiles(requestDto.getMediaFiles(), savedPost);
        List<String> hashtagNames = saveHashtagNames(requestDto.getHashtags(), savedPost);


        // PostResponseDto 생성
        PostResponseDto responseDto = PostResponseDto.builder()
                .postId(savedPost.getPostId())
                .userId(member.getId())
                .userImg(member.getImageURL())
                .userName(member.getUsername())
                .date(savedPost.getCreatedAt())
                .location(savedPost.getPostLocation())
                .temperature(21F)
                .mediaUrls(mediaUrls)
                .content(savedPost.getPostContent())
                .liked(false)
                .likedCount(0)
                .hashtagNames(hashtagNames)
                .build();

        return responseDto;
    }

    private List<String> saveMediaFiles(List<MultipartFile> mediaFiles, Post post) {
        if (mediaFiles == null) {
            return Collections.emptyList();
        }

        List<String> mediaUrls = s3Uploader.upload(mediaFiles); // 수정
        // 각 URL을 Post 엔티티에 추가
        for (String mediaUrl : mediaUrls) {
            MediaFile mediaFile = new MediaFile(mediaUrl, post);
            post.addMediaFile(mediaFile);
        }

        return mediaUrls;
    }

    private List<String> saveHashtagNames(List<String> hashtagNames, Post post) {
        List<String> savedHashtagNames = new ArrayList<>();

        for (String hashtagName : hashtagNames) {
            // 데이터베이스에 해시태그가 이미 존재하는지 확인
            Hashtag existingHashtag = hashtagRepository.findHashtagByHashtagName(hashtagName);

            // 존재하지 않으면 생성 및 저장
            if (existingHashtag == null) {
                existingHashtag = new Hashtag(hashtagName);
                existingHashtag = hashtagRepository.save(existingHashtag);
            }

            // PostHashtag 생성 및 저장
            PostHashtag postHashtag = new PostHashtag(post, existingHashtag);
            postHashtagRepository.save(postHashtag);

            // 저장된 해시태그의 이름을 리스트에 추가
            savedHashtagNames.add(existingHashtag.getHashtagName());
        }

        return savedHashtagNames;
    }



}
