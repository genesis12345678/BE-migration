package com.example.project3.Entity.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Member implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
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

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;

    @Builder
    public Member(String name, String email, String password, String address,
                  String imageURL, String nickName, String gender, String phoneNumber,
                  Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.imageURL = imageURL;
        this.nickName = nickName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    @Override // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(role.getValue()));
        return roles;
    }

    @Override // 사용자의 고유한 값 반환
    public String getUsername() {
        return email;
    }

    @Override // 계정 만료 여부 반환
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override // 계정 잠금 여부 반환
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override // 패스워드 만료 여부 반환
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override // 계정 사용 가능 여부 반환
    public boolean isEnabled() {
        return true;
    }
}