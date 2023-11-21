package com.example.project3.dto.response;

import com.example.project3.Entity.member.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class MemberInfoResponse {

    private Long memberId;
    private String name;
    private String email;
    private String address;
    private String imageUrl;
    private String nickName;
    private String message;
    private String socialId;
    private SocialType socialType;
    private List<SimplifiedPostResponse> simplifiedPostResponseList;
}