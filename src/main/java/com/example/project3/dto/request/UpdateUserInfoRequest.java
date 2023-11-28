package com.example.project3.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ApiModel(value = "회원 정보 수정 요청 정보")
public class UpdateUserInfoRequest {

    @ApiModelProperty(value = "회원 주소" , example = "서울특별시")
    private String address;

    @ApiModelProperty(value = "회원 별명" , example = "별명1")
    private String nickName;

    @ApiModelProperty(value = "회원 한 줄 메시지" , example = "날씨가 좋습니다.")
    private String message;
}
