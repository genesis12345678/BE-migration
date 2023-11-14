package com.example.project3.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class SocialUserSignupRequest {

    private String message;
    private String address;
    private String nickName;
}
