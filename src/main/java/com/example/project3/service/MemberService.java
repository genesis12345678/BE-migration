package com.example.project3.service;

import com.example.project3.Entity.MediaFile;
import com.example.project3.Entity.member.Member;
import com.example.project3.Entity.member.Role;
import com.example.project3.config.jwt.TokenProvider;
import com.example.project3.dto.request.SignupRequest;
import com.example.project3.dto.request.SocialUserSignupRequest;
import com.example.project3.dto.response.MemberInfoResponse;
import com.example.project3.dto.response.SimplifiedPostResponse;
import com.example.project3.exception.FileUploadException;
import com.example.project3.repository.MemberRepository;
import com.example.project3.repository.PostRepository;
import com.example.project3.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;
    private final PostRepository postRepository;
    private final S3Uploader s3Uploader;
    private final RedisUtil redisUtil;


    public static final String DEFAULT_IMAGE_URL = "https://meatwiki.nii.ac.jp/confluence/images/icons/profilepics/anonymous.png";

    public ResponseEntity<String> signup(SignupRequest request, MultipartFile file) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return memberRepository.findByEmail(request.getEmail())
                .map(member -> {
                    log.error("중복 이메일, 이미 가입된 정보임.");
                    return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
                })
                .orElseGet(() -> {
                    try {

                        String imageURL = (!file.isEmpty()) ? s3Uploader.uploadProfileImage(file) : DEFAULT_IMAGE_URL;

                        memberRepository.save(Member.builder()
                                .name(request.getUserName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .address(request.getAddress())
                                .imageURL(imageURL)
                                .nickName(request.getNickName())
                                .message(request.getMessage())
                                .role(Role.USER)
                                .build());
                        log.info("회원정보가 저장되었습니다.");

                    } catch (IOException e) {
                        log.error("파일 업로드 중 오류 발생");
                        throw new FileUploadException("파일 업로드 중 오류 발생", e);
                    }

                    return new ResponseEntity<>("Signup Successful", HttpStatus.OK);
                });
    }


    public void signupSocialUser(String token, SocialUserSignupRequest request, HttpServletResponse response) {
        log.info("소셜 유저 회원가입 실행");

        String email = tokenProvider.getMemberEmail(token);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);

        member.signupSocialUser(request.getMessage(), request.getAddress(), request.getNickName());

        String accessToken = tokenService.createAccessToken(email);
        String refreshToken = tokenService.createRefreshToken();

        member.updateRefreshToken(refreshToken);

        memberRepository.save(member);
        log.info("추가로 입력받은 정보로 GUEST -> USER로 변환하고 회원가입을 마무리합니다.");

        tokenService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
    }

    public MemberInfoResponse getMemberInfo(String username, Pageable pageable) {

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(EntityNotFoundException::new);

        Page<SimplifiedPostResponse> simplifiedPosts = postRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable)
                .map(SimplifiedPostResponse::new);

        return MemberInfoResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .imageUrl(member.getImageURL())
                .address(member.getAddress())
                .message(member.getMessage())
                .nickName(member.getNickName())
                .socialId(member.getSocialId())
                .socialType(member.getSocialType())
                .simplifiedPostResponseList(simplifiedPosts.getContent())
                .build();

    }

    @Transactional
    public void deleteAccount(String email) {

        memberRepository.findByEmail(email)
                .ifPresent(member -> {

                    if (member.getSocialType() != null) {
                        deleteSNSFile(member);

                    } else if (!member.getImageURL().equals(DEFAULT_IMAGE_URL)) {
                        s3Uploader.delete(member.getImageURL());
                        deleteSNSFile(member);
                    }

                    memberRepository.delete(member);
                    log.info("{} 계정 정보를 삭제합니다.", email);
                });
    }

    private void deleteSNSFile(Member member) {
        member.getPosts().forEach(post -> {
            List<MediaFile> mediaFiles = post.getMediaFiles();
            mediaFiles.forEach(mediaFile -> {
                log.info("S3버킷에서 파일을 삭제합니다.");
                s3Uploader.delete(mediaFile.getFileUrl());
            });
        });
    }


    @Transactional
    public void logout(UserDetails userDetails, String accessToken) {
        try {
            String token = accessToken.substring(7);
            String email = userDetails.getUsername();

            memberRepository.findByEmail(email)
                    .ifPresent(member -> {
                        log.info("로그아웃 되어 {}님의 RefreshToken을 지웁니다.", member.getName());
                        member.clearRefreshToken();

                        SecurityContextHolder.clearContext();
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            log.info("현재 인증객체 삭제");
                        }
                        redisUtil.setBlackList(token, "accessToken", 5);
                        log.info("액세스 토큰 블랙리스트 추가");

                    });
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}

