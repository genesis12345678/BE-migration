package com.example.project3.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {

    @NotBlank(message = "Username is required") // "", " ", null 허용 안함.
    private String userName;

    @NotBlank(message = "Email is required") // "", " ", null 허용 안함.
    @Email(message = "Invalid Email")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*?])(?=.*[a-zA-Z]).{8,20}$",
            message = "8 ~ 20자, 최소 한개의 특수문자와 숫자, 영문 알파벳을 포함해야 함.")
    private String password;

    @NotBlank(message = "Address is required")
    private String address;

    private String imageURL;

    private String nickName;

    @Pattern(regexp = "^(MALE|FEMALE)?$", message = "MALE or FEMALE")
    private String gender;

    @Pattern(regexp = "^010\\d{8}$", message = "Invalid phone number") // 010xxxxxxxx 만 허용
    private String phoneNumber;
}