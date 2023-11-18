package com.example.project3.service;

import com.example.project3.Entity.*;
import com.example.project3.Entity.member.Member;
import com.example.project3.dto.request.PostRequestDto;
import com.example.project3.dto.response.PostResponseDto;
import com.example.project3.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostLikedRepository postLikedRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final HashtagRepository hashtagRepository;
    private final S3Uploader s3Uploader;


    @Transactional
    //public PostResponseDto createPost(String username, PostRequestDto requestDto) {
    public String createPost(String username, PostRequestDto requestDto) {

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(EntityNotFoundException::new);

        Post post = Post.builder()
                .postLocation(requestDto.getLocation())
                .postContent(requestDto.getContent())
                .postTemperature(requestDto.getTemperature())
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

//        PostResponseDto responseDto = PostResponseDto.builder()
//                .postId(savedPost.getPostId())
//                .userId(member.getId())
//                .userImg(member.getImageURL())
//                .userName(member.getName())
//                .date(savedPost.getCreatedAt())
//                .location(savedPost.getPostLocation())
//                .temperature(21F)
//                .mediaUrls(mediaUrls)
//                .content(savedPost.getPostContent())
//                .liked(false)
//                .likedCount(0)
//                .hashtagNames(hashtagNames)
//                .build();
//
//        return responseDto;
        // 등록 완료 메시지
        String message = "게시물이 성공적으로 등록되었습니다.";
        return message;

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



    @Transactional
    public boolean toggleLike(Long postId, String userEmail) {
        if (userEmail == null) {
            // 사용자가 로그인되지 않았습니다. false를 반환합니다.
            return false;
        }

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        PostLiked postLiked = postLikedRepository.findByPostAndMember(post, member);

        if (postLiked != null) {
            // 이미 좋아요를 눌렀으면 취소
            postLikedRepository.delete(postLiked);
            post.decreaseCountLiked();
            return false;
        } else {
            // 좋아요를 누르지 않았으면 좋아요 추가
            postLikedRepository.save(PostLiked.builder().post(post).member(member).liked(true).build());
            post.increaseCountLiked();
            return true;
        }
    }

    public Page<PostResponseDto> getAllPostList(Long lastPostId, Pageable pageable, String userEmail) {
        // 게시글을 페이징하여 가져오기
        Page<Post> posts = postRepository.findByPostIdLessThanOrderByCreatedAtDesc(lastPostId, pageable);

        // Page<Post>를 Page<PostResponseDto>로 변환
        Page<PostResponseDto> postResponseDtoPage = posts.map(post -> createPostResponseDto(post, userEmail));

        return postResponseDtoPage;
    }

    private PostResponseDto createPostResponseDto(Post post, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));


        List<String> mediaUrls = post.getMedias().stream()
                .map(MediaFile::getFileUrl)
                .collect(Collectors.toList());

        boolean isPostLiked = postLikedRepository.existsByPostAndMember(post, member);

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .userId(post.getMember().getId())
                .userImg(post.getMember().getImageURL())
                .userName(post.getMember().getName())
                .date(post.getCreatedAt())
                .location(post.getPostLocation())
                .temperature(post.getPostTemperature())
                .mediaUrls(mediaUrls)
                .content(post.getPostContent())
                .liked(isPostLiked)
                .likedCount(post.getCountLiked())
                .hashtagNames(post.getPostHashtags().stream()
                        .map(PostHashtag::getHashtag)
                        .map(Hashtag::getHashtagName)
                        .collect(Collectors.toList()))
                .build();
    }

    public Page<PostResponseDto> getAllPostListForAnonymous(Long lastPostId, Pageable pageable) {
        // 사용자가 로그인하지 않은 경우의 로직을 구현합니다.
        // 예를 들어, 좋아요 상태를 기본값으로 설정하거나 필요에 따라 다르게 처리할 수 있습니다.

        // 게시글을 페이징하여 가져오기
        Page<Post> posts = postRepository.findByPostIdLessThanOrderByCreatedAtDesc(lastPostId, pageable);

        // Page<Post>를 Page<PostResponseDto>로 변환
        Page<PostResponseDto> postResponseDtoPage = posts.map(post -> createPostResponseDtoForAnonymous(post));

        return postResponseDtoPage;
    }

    private PostResponseDto createPostResponseDtoForAnonymous(Post post) {
        // 사용자가 로그인하지 않은 경우에 대한 PostResponseDto를 생성하는 로직을 구현합니다.
        // 예를 들어, 좋아요 상태를 기본값으로 설정하거나 필요에 따라 다르게 처리할 수 있습니다.

        List<String> mediaUrls = post.getMedias().stream()
                .map(MediaFile::getFileUrl)
                .collect(Collectors.toList());

        // 로그인하지 않은 경우에는 좋아요 상태를 기본값으로 설정
        boolean isPostLiked = false;

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .userId(post.getMember().getId())
                .userImg(post.getMember().getImageURL())
                .userName(post.getMember().getName())
                .date(post.getCreatedAt())
                .location(post.getPostLocation())
                .temperature(post.getPostTemperature())
                .mediaUrls(mediaUrls)
                .content(post.getPostContent())
                .liked(isPostLiked)
                .likedCount(post.getCountLiked())
                .hashtagNames(post.getPostHashtags().stream()
                        .map(PostHashtag::getHashtag)
                        .map(Hashtag::getHashtagName)
                        .collect(Collectors.toList()))
                .build();
    }



}