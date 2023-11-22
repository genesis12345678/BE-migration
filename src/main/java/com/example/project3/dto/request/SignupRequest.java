package com.example.project3.dto.request;

import lombok.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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
    @Email(message = "이메일 형식을 맞춰주세요.")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*?])(?=.*[a-zA-Z]).{8,20}$",
            message = "8 ~ 20자, 최소 한개의 특수문자와 숫자, 영문 알파벳을 포함해야 함.")
    private String password;

    @NotBlank(message = "Address is required")
    private String address;

    private String nickName;

    private String message;
}