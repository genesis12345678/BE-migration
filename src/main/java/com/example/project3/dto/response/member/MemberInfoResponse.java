package com.example.project3.dto.response.member;

import com.example.project3.entity.member.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "회원 정보 조회 응답")
public record MemberInfoResponse(
        @Schema(description = "회원ID", example = "1") Long memberId,
        @Schema(description = "회원이름", example = "사용자1") String name,
        @Schema(description = "회원이메일", example = "test@email.com") String email,
        @Schema(description = "회원주소", example = "서울특별시") String address,
        @Schema(description = "회원이미지URL", example = "https://meatwiki.nii.ac.jp/confluence/images/icons/profilepics/anonymous.png") String imageURL,
        @Schema(description = "회원별명", example = "별명1") String nickName,
        @Schema(description = "회원 한 줄 메시지", example = "날씨가 좋습니다.") String message,
        @Schema(description = "소셜유저 인 경우에만 나타남") String socialId,
        @Schema(description = "소셜유저 인 경우에만 나타남", example = "KAKAO or GOOGLE") SocialType socialType,
        @Schema(description = "등록했던 글") List<SimplifiedPostResponse> simplifiedPostResponseList) {

}