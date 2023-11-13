package com.example.project3.Entity.member;

import lombok.*;

import javax.persistence.*;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Builder
@AllArgsConstructor
public class Member{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    private String imageURL;

    private String nickName;

    private String gender;

    private String message;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    private String refreshToken;

    // 자체 로그인용 빌더
    @Builder
    public Member (String name, String email, String password, String address,
                  String imageURL, String nickName, String gender, String message,
                  Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.imageURL = imageURL;
        this.nickName = nickName;
        this.gender = gender;
        this.message = message;
        this.role = role;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void authorizeMember() {
        this.role = Role.USER;
    }

}