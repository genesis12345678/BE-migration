package com.example.project3.domain;

import lombok.*;

import javax.persistence.*;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String imageURL;

    private String nickName;

    private String gender;

    @Column(length = 11)
    private String phoneNumber;

    @Builder
    public Member(String name, String email, String password, String address,
                  String imageURL, String nickName, String gender, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.imageURL = imageURL;
        this.nickName = nickName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
}