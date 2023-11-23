package com.example.project3.service;

import com.example.project3.Entity.*;
import com.example.project3.Entity.member.Member;
import com.example.project3.dto.request.PostRequestDto;
import com.example.project3.dto.request.PostUpdateRequestDto;
import com.example.project3.dto.response.PostResponseDto;
import com.example.project3.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostLikedRepository postLikedRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final MediaFileRepository mediaFileRepository;
    private final HashtagRepository hashtagRepository;
    private final S3Uploader s3Uploader;


    @Transactional
    public Long createPost(String username, PostRequestDto requestDto) {

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(()->new IllegalArgumentException("가입된 정보가 없는 이메일"));

        Post post = Post.builder()
                .postLocation(requestDto.getLocation())
                .postContent(requestDto.getContent())
                .postTemperature(requestDto.getTemperature())
                .member(member)
                .build();

        // DB에 저장
        Post savedPost = postRepository.save(post);

        // MediaFiles 처리
        saveMediaFiles(requestDto.getMediaFiles(), savedPost);
        // Hashtag 처리
        saveHashtagNames(requestDto.getHashtags(), savedPost);


        return savedPost.getPostId();

    }

    private List<MediaFile> saveMediaFiles(List<MultipartFile> mediaFiles, Post post) {
        if (mediaFiles == null) {
            return Collections.emptyList();
        }

        List<String> mediaUrls = s3Uploader.upload(mediaFiles); // 수정
        log.info(String.valueOf(mediaUrls));
        // 각 URL을 Post 엔터티에 추가
        if (post.getMediaFiles() == null) {
            post.setMedias();
        }
        List<MediaFile> mediaFileList = new ArrayList<>();

        // 각 URL을 Post 엔티티에 추가하고 MediaFile 객체를 리스트에 추가
        for (String mediaUrl : mediaUrls) {
            MediaFile mediaFile = new MediaFile(mediaUrl, post);
            post.addMediaFile(mediaFile);
            mediaFileList.add(mediaFile);
        }

        return mediaFileList;
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
        Member member = (userEmail != null)
                ? memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail))
                : post.getMember(); // 사용자가 로그인하지 않은 경우 게시글 작성자 정보 사용

        List<String> mediaUrls = post.getMediaFiles().stream()
                .map(MediaFile::getFileUrl)
                .collect(Collectors.toList());

        boolean isPostLiked = userEmail != null && postLikedRepository.existsByPostAndMember(post, member); // 사용자가 로그인하지 않은 경우 좋아요 여부 false로 설정


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
                        .distinct()
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

        List<String> mediaUrls = post.getMediaFiles().stream()
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

    public PostResponseDto getPostById(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        return createPostResponseDto(post, userEmail);
    }




    @Transactional
    public PostResponseDto updatePost2(Long postId, String username, PostUpdateRequestDto request) {

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
//        post.update(request);
        // 기존 이미지와 넘어온 이미지 비교
        List<String> updateOriginalImages = request.getOriginalImages();
        List<String> productImages = getExistingImageUrls(post.getMediaFiles());
        // 원래 있던 이미지에서 빠진 이미지를 찾아냄
        List<String> removeImages = pickUpRemoveProductImages(productImages, updateOriginalImages);

        // 레파지토리에서 이미지 삭제, S3에서 빠진 이미지 파일 삭제
        for (String deletedImage : removeImages) {
            mediaFileRepository.deleteByPostIdAndFileUrl(postId, deletedImage);
            s3Uploader.delete(deletedImage);
        }

        // 새로운 이미지 파일 추가
        addPostImages(post, request.getNewPostImages());

        updateHashtags(post, request.getHashtags());
        //updateHashtags1(post, requestDto.getHashtags());

        post.update(request);
        // 수정된 게시글 저장
        postRepository.save(post);


        // 영속성 컨텍스트에서 업데이트된 게시글을 다시 조회하여 응답 DTO 생성
        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));


        // 수정된 게시글의 응답 DTO 생성
        return createPostResponseDto(updatedPost, username);
    }

    private List<String> getExistingImageUrls(List<MediaFile> existingImages) {
        return existingImages.stream()
                .map(MediaFile::getFileUrl)
                .collect(Collectors.toList());
    }
    private List<String> pickUpRemoveProductImages(List<String> originImage, List<String> updateImage) {
        return originImage.stream()
                .filter(image -> !updateImage.contains(image))
                .collect(Collectors.toList());
    }

    private void addPostImages(Post post, List<MultipartFile> mediaFiles) {
        List<String> postMediaUrls = s3UploadAndConverter(mediaFiles);
        // 기존 이미지 파일과 새로 추가된 이미지 파일의 중복을 방지하기 위해 새로운 이미지 추가 전에 모든 기존 이미지를 삭제
        post.getMediaFiles().clear();


        for (String mediaUrl : postMediaUrls) {
            MediaFile mediaFile = new MediaFile(mediaUrl);
            //post.addPostImage(mediaFile);
            post.addMediaFile(mediaFile); // addMediaFile 메서드로 추가하도록 수정
        }
    }
    public List<String> s3UploadAndConverter(List<MultipartFile> multipartFiles) {
        List<String> mediaUrls = s3Uploader.upload(multipartFiles);
        return mediaUrls;
    }
    private void updateHashtags(Post post, List<String> newHashtags) {
        // 기존의 해시태그를 가져옵니다.
        List<PostHashtag> existingPostHashtags = post.getPostHashtags();

        // 새로운 해시태그를 추가합니다.
        for (String newHashtag : newHashtags) {
            // 데이터베이스에 해시태그가 이미 존재하는지 확인
            Hashtag existingHashtag = hashtagRepository.findHashtagByHashtagName(newHashtag);

            // 존재하지 않으면 생성 및 저장
            if (existingHashtag == null) {
                existingHashtag = new Hashtag(newHashtag);
                existingHashtag = hashtagRepository.save(existingHashtag);
            }

            // 중복되지 않는 해시태그만 추가합니다.
            if (!existingPostHashtags.stream().anyMatch(postHashtag -> postHashtag.getHashtag().getHashtagName().equals(newHashtag))) {
                // PostHashtag 생성 및 저장
                PostHashtag postHashtag = new PostHashtag(post, existingHashtag);
                postHashtagRepository.save(postHashtag);
                existingPostHashtags.add(postHashtag);
            }
        }
    }



}