package com.example.project3.dto.response;

import com.example.project3.Entity.member.SocialType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@ApiModel(value = "회원 정보 조회 응답")
public class MemberInfoResponse {

    @ApiModelProperty(value = "회원ID", example = "1")
    private Long memberId;

    @ApiModelProperty(value = "회원이름", example = "사용자1")
    private String name;

    @ApiModelProperty(value = "회원이메일", example = "test@email.com")
    private String email;

    @ApiModelProperty(value = "회원주소", example = "서울특별시")
    private String address;

    @ApiModelProperty(value = "회원이미지URL", example = "https://meatwiki.nii.ac.jp/confluence/images/icons/profilepics/anonymous.png")
    private String imageUrl;

    @ApiModelProperty(value = "회원별명", example = "별명1")
    private String nickName;

    @ApiModelProperty(value = "회원 한 줄 메시지", example = "날씨가 좋습니다.")
    private String message;

    @ApiModelProperty(value = "소셜유저 인 경우에만 나타남")
    private String socialId;

    @ApiModelProperty(value = "소셜유저 인 경우에만 나타남", example = "KAKAO or GOOGLE")
    private SocialType socialType;

    private List<SimplifiedPostResponse> simplifiedPostResponseList;
}