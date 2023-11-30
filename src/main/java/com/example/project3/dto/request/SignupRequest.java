package com.example.project3.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ApiModel(value = "회원가입 요청 정보")
public class SignupRequest extends UpdateUserInfoRequest{

    @ApiModelProperty(value = "이름", required = true, example = "사용자1")
    @NotBlank(message = "Username is required") // "", " ", null 허용 안함.
    private String userName;

    @ApiModelProperty(value = "이메일(유효성 검사 : 이메일 형식)", required = true, example = "test@email.com")
    @NotBlank(message = "Email is required") // "", " ", null 허용 안함.
    @Email(message = "이메일 형식을 맞춰주세요.")
    private String email;

    @ApiModelProperty(value = "비밀번호(유효성 검사 : 8 ~ 20자, 최소 한개의 특수문자와 숫자, 영문 알파벳을 포함)", required = true, example = "password12@")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*?])(?=.*[a-zA-Z]).{8,20}$",
            message = "8 ~ 20자, 최소 한개의 특수문자와 숫자, 영문 알파벳을 포함해야 함.")
    private String password;

    @Builder
    // 테스트 코드용
    public SignupRequest(String username, String email, String password, String address, String nickName, String message) {
        super(address,nickName,message);
        this.password = password;
        this.userName = username;
        this.email = email;
    }
}